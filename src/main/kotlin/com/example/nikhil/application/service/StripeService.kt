package com.example.nikhil.application.service

import com.example.nikhil.infrastructure.kafka.event.NotificationEvent
import com.example.nikhil.infrastructure.kafka.event.NotificationType
import com.example.nikhil.infrastructure.kafka.event.OrderAction
import com.example.nikhil.infrastructure.kafka.event.OrderEvent
import com.example.nikhil.infrastructure.kafka.producer.KafkaProducerService
import com.example.nikhil.infrastructure.web.dto.CheckoutSessionResponse
import com.example.nikhil.infrastructure.web.dto.CreateCheckoutRequest
import com.example.nikhil.infrastructure.web.dto.CreatePaymentIntentRequest
import com.example.nikhil.infrastructure.web.dto.PaymentIntentResponse
import com.stripe.exception.SignatureVerificationException
import com.stripe.model.Event
import com.stripe.model.PaymentIntent
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.PaymentIntentCreateParams
import com.stripe.param.checkout.SessionCreateParams
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * Stripe Payment Service
 * Handles payment processing with Stripe
 */
@Service
class StripeService(
    private val cartService: CartService,
    private val kafkaProducerService: KafkaProducerService
) {
    private val logger = LoggerFactory.getLogger(StripeService::class.java)

    @Value("\${stripe.webhook.secret}")
    private lateinit var webhookSecret: String

    @Value("\${stripe.success.url}")
    private lateinit var successUrl: String

    @Value("\${stripe.cancel.url}")
    private lateinit var cancelUrl: String

    /**
     * Create a Stripe Checkout Session for cart checkout
     */
    fun createCheckoutSession(request: CreateCheckoutRequest): CheckoutSessionResponse {
        logger.info("Creating checkout session for cart: ${request.cartId}")

        val cart = cartService.getCartById(request.cartId)
        val totalAmount = calculateTotalInCents(cart.totalPrice)

        val lineItems = cart.items.map { item ->
            SessionCreateParams.LineItem.builder()
                .setQuantity(item.quantity.toLong())
                .setPriceData(
                    SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(request.currency)
                        .setUnitAmount((item.productPrice?.multiply(BigDecimal(100)))?.toLong() ?: 0L)
                        .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(item.productName ?: "Product")
                                .build()
                        )
                        .build()
                )
                .build()
        }

        val params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl("$successUrl?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(cancelUrl)
            .addAllLineItem(lineItems)
            .putMetadata("cartId", request.cartId.toString())
            .putMetadata("userId", request.userId.toString())
            .build()

        val session = Session.create(params)

        logger.info("Checkout session created: ${session.id}")

        // Publish checkout session created event
        publishPaymentEvent(
            userId = request.userId,
            cartId = request.cartId,
            eventType = "CHECKOUT_SESSION_CREATED",
            action = OrderAction.CREATED,
            amount = BigDecimal(totalAmount).divide(BigDecimal(100)),
            details = mapOf(
                "sessionId" to session.id,
                "checkoutUrl" to session.url,
                "itemCount" to cart.items.size
            )
        )

        return CheckoutSessionResponse(
            sessionId = session.id,
            checkoutUrl = session.url,
            amount = totalAmount
        )
    }

    /**
     * Create a Payment Intent for custom payment flow
     */
    fun createPaymentIntent(request: CreatePaymentIntentRequest): PaymentIntentResponse {
        logger.info("Creating payment intent for amount: ${request.amount} ${request.currency}")

        val paramsBuilder = PaymentIntentCreateParams.builder()
            .setAmount(request.amount)
            .setCurrency(request.currency)
            .addPaymentMethodType("card")

        request.description?.let { paramsBuilder.setDescription(it) }
        request.userId?.let { paramsBuilder.putMetadata("userId", it.toString()) }

        val paymentIntent = PaymentIntent.create(paramsBuilder.build())

        logger.info("Payment intent created: ${paymentIntent.id}")

        return PaymentIntentResponse(
            paymentIntentId = paymentIntent.id,
            clientSecret = paymentIntent.clientSecret,
            amount = paymentIntent.amount,
            currency = paymentIntent.currency,
            status = paymentIntent.status
        )
    }

    /**
     * Retrieve a checkout session by ID
     */
    fun getCheckoutSession(sessionId: String): Session {
        return Session.retrieve(sessionId)
    }

    /**
     * Handle Stripe webhook events
     */
    fun handleWebhookEvent(payload: String, signature: String): String {
        logger.info("Processing Stripe webhook event")

        val event: Event = try {
            Webhook.constructEvent(payload, signature, webhookSecret)
        } catch (e: SignatureVerificationException) {
            logger.error("Webhook signature verification failed: ${e.message}")
            throw IllegalArgumentException("Invalid signature")
        }

        logger.info("Received Stripe event: ${event.type}")

        when (event.type) {
            "checkout.session.completed" -> handleCheckoutSessionCompleted(event)
            "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event)
            "payment_intent.payment_failed" -> handlePaymentIntentFailed(event)
            "charge.succeeded" -> handleChargeSucceeded(event)
            "charge.refunded" -> handleChargeRefunded(event)
            else -> logger.info("Unhandled event type: ${event.type}")
        }

        return "Event processed: ${event.type}"
    }

    private fun handleCheckoutSessionCompleted(event: Event) {
        val session = event.dataObjectDeserializer.`object`.orElse(null) as? Session
        session?.let {
            val cartId = it.metadata["cartId"]?.toLongOrNull()
            val userId = it.metadata["userId"]?.toLongOrNull()

            logger.info("Checkout completed for cart: $cartId, user: $userId, amount: ${it.amountTotal}")

            // Publish order event to Kafka
            if (cartId != null && userId != null) {
                try {
                    kafkaProducerService.publishOrderEvent(
                        OrderEvent(
                            eventType = "ORDER_CREATED",
                            userId = userId,
                            cartId = cartId,
                            action = OrderAction.CREATED,
                            totalAmount = BigDecimal(it.amountTotal).divide(BigDecimal(100)),
                            details = mapOf(
                                "sessionId" to it.id,
                                "paymentStatus" to it.paymentStatus
                            )
                        )
                    )

                    // Also publish notification event
                    kafkaProducerService.publishNotificationEvent(
                        NotificationEvent(
                            userId = userId,
                            email = "user@example.com", // Would get from user service in production
                            notificationType = NotificationType.EMAIL,
                            subject = "Order Confirmation",
                            message = "Your order has been placed successfully! Order total: ${it.amountTotal / 100}",
                            metadata = mapOf(
                                "orderId" to it.id,
                                "amount" to it.amountTotal
                            )
                        )
                    )

                    // Complete the cart checkout
                    try {
                        cartService.completeCheckout(cartId, it.id)
                    } catch (e: Exception) {
                        logger.warn("Failed to complete cart checkout: ${e.message}")
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to publish order event: ${e.message}")
                }
            }
        }
    }

    private fun handlePaymentIntentSucceeded(event: Event) {
        val paymentIntent = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent
        paymentIntent?.let {
            logger.info("Payment succeeded: ${it.id}, amount: ${it.amount}")

            val userId = it.metadata["userId"]?.toLongOrNull() ?: 0L

            // Publish payment success event
            publishPaymentEvent(
                userId = userId,
                cartId = 0L,
                eventType = "PAYMENT_SUCCEEDED",
                action = OrderAction.CONFIRMED,
                amount = BigDecimal(it.amount).divide(BigDecimal(100)),
                details = mapOf(
                    "paymentIntentId" to it.id,
                    "status" to it.status
                )
            )
        }
    }

    private fun handlePaymentIntentFailed(event: Event) {
        val paymentIntent = event.dataObjectDeserializer.`object`.orElse(null) as? PaymentIntent
        paymentIntent?.let {
            logger.warn("Payment failed: ${it.id}, last error: ${it.lastPaymentError?.message}")

            val userId = it.metadata["userId"]?.toLongOrNull() ?: 0L

            // Publish payment failed event
            publishPaymentEvent(
                userId = userId,
                cartId = 0L,
                eventType = "PAYMENT_FAILED",
                action = OrderAction.CANCELLED,
                amount = BigDecimal(it.amount).divide(BigDecimal(100)),
                details = mapOf(
                    "paymentIntentId" to it.id,
                    "error" to (it.lastPaymentError?.message ?: "Unknown error")
                )
            )
        }
    }

    private fun handleChargeSucceeded(event: Event) {
        logger.info("Charge succeeded")
        // Publish charge success event
        kafkaProducerService.publishMessage(
            "order-events",
            "charge",
            """{"eventType": "CHARGE_SUCCEEDED", "timestamp": "${java.time.LocalDateTime.now()}"}"""
        )
    }

    private fun handleChargeRefunded(event: Event) {
        logger.info("Charge refunded")
        // Publish refund event
        kafkaProducerService.publishMessage(
            "order-events",
            "refund",
            """{"eventType": "CHARGE_REFUNDED", "timestamp": "${java.time.LocalDateTime.now()}"}"""
        )
    }

    private fun calculateTotalInCents(total: BigDecimal): Long {
        return total.multiply(BigDecimal(100)).toLong()
    }

    /**
     * Helper method to publish payment-related Kafka events
     */
    private fun publishPaymentEvent(
        userId: Long,
        cartId: Long,
        eventType: String,
        action: OrderAction,
        amount: BigDecimal?,
        details: Map<String, Any>
    ) {
        try {
            kafkaProducerService.publishOrderEvent(
                OrderEvent(
                    eventType = eventType,
                    userId = userId,
                    cartId = cartId,
                    action = action,
                    totalAmount = amount,
                    details = details
                )
            )
            logger.debug("Published $eventType event")
        } catch (e: Exception) {
            logger.warn("Failed to publish $eventType event: ${e.message}")
        }
    }
}

