package com.example.nikhil.infrastructure.kafka.consumer

import com.example.nikhil.infrastructure.kafka.event.NotificationEvent
import com.example.nikhil.infrastructure.kafka.event.OrderEvent
import com.example.nikhil.infrastructure.kafka.event.UserEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

/**
 * Kafka Consumer Service
 * Consumes events from Kafka topics
 * Only enabled when Kafka is available
 */
@Service
@ConditionalOnProperty(name = ["spring.kafka.enabled"], havingValue = "true", matchIfMissing = false)
class KafkaConsumerService(
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(KafkaConsumerService::class.java)

    /**
     * Listen to user events
     */
    @KafkaListener(
        topics = ["\${app.kafka.topics.user-events:user-events}"],
        groupId = "\${spring.kafka.consumer.group-id:spring-boot-group}"
    )
    fun consumeUserEvent(message: String) {
        try {
            val event = objectMapper.readValue(message, UserEvent::class.java)
            logger.info("Received user event: ${event.action} for user ${event.userId}")

            // Process the event based on action
            when (event.action) {
                com.example.nikhil.infrastructure.kafka.event.UserAction.REGISTERED -> handleUserRegistered(event)
                com.example.nikhil.infrastructure.kafka.event.UserAction.LOGGED_IN -> handleUserLoggedIn(event)
                com.example.nikhil.infrastructure.kafka.event.UserAction.PASSWORD_CHANGED -> handlePasswordChanged(event)
                else -> logger.info("Unhandled user action: ${event.action}")
            }
        } catch (e: Exception) {
            logger.error("Failed to process user event: ${e.message}", e)
        }
    }

    /**
     * Listen to order events
     */
    @KafkaListener(
        topics = ["\${app.kafka.topics.order-events:order-events}"],
        groupId = "\${spring.kafka.consumer.group-id:spring-boot-group}"
    )
    fun consumeOrderEvent(message: String) {
        try {
            val event = objectMapper.readValue(message, OrderEvent::class.java)
            logger.info("📦 Received order event: [${event.eventType}] for cart ${event.cartId}, user ${event.userId}")

            // Process the event based on eventType
            when (event.eventType) {
                // Cart events
                "CART_CREATED" -> handleCartCreated(event)
                "ITEM_ADDED" -> handleItemAdded(event)
                "ITEM_REMOVED" -> handleItemRemoved(event)
                "ITEM_QUANTITY_UPDATED" -> handleItemQuantityUpdated(event)
                "CART_CLEARED" -> handleCartCleared(event)
                "CART_DELETED" -> handleCartDeleted(event)

                // Checkout events
                "CHECKOUT_STARTED" -> handleCheckoutStarted(event)
                "CHECKOUT_SESSION_CREATED" -> handleCheckoutSessionCreated(event)

                // Payment events
                "PAYMENT_SUCCEEDED" -> handlePaymentSucceeded(event)
                "PAYMENT_FAILED" -> handlePaymentFailed(event)

                // Order events
                "ORDER_CREATED" -> handleOrderCreated(event)
                "ORDER_COMPLETED" -> handleOrderCompleted(event)

                else -> logger.info("📌 Unhandled event type: ${event.eventType}")
            }
        } catch (e: Exception) {
            logger.error("❌ Failed to process order event: ${e.message}", e)
        }
    }

    /**
     * Listen to notification events
     */
    @KafkaListener(
        topics = ["\${app.kafka.topics.notification-events:notification-events}"],
        groupId = "\${spring.kafka.consumer.group-id:spring-boot-group}"
    )
    fun consumeNotificationEvent(message: String) {
        try {
            val event = objectMapper.readValue(message, NotificationEvent::class.java)
            logger.info("Received notification event: ${event.notificationType} for user ${event.userId}")

            // Process notification
            sendNotification(event)
        } catch (e: Exception) {
            logger.error("Failed to process notification event: ${e.message}", e)
        }
    }

    // Event handlers
    private fun handleUserRegistered(event: UserEvent) {
        logger.info("✅ Processing user registration for: ${event.email}")
    }

    private fun handleUserLoggedIn(event: UserEvent) {
        logger.info("🔐 User logged in: ${event.email}")
    }

    private fun handlePasswordChanged(event: UserEvent) {
        logger.info("🔑 Password changed for user: ${event.email}")
    }

    // Cart event handlers
    private fun handleCartCreated(event: OrderEvent) {
        logger.info("🛒 CART CREATED | Cart ID: ${event.cartId} | User ID: ${event.userId}")
        logger.info("   └─ Details: ${event.details}")
    }

    private fun handleItemAdded(event: OrderEvent) {
        val productName = event.details?.get("productName") ?: "Unknown"
        val quantity = event.details?.get("quantity") ?: 0
        logger.info("➕ ITEM ADDED TO CART | Cart ID: ${event.cartId} | Product: $productName | Qty: $quantity")
        logger.info("   └─ User ID: ${event.userId}")
    }

    private fun handleItemRemoved(event: OrderEvent) {
        val productName = event.details?.get("productName") ?: "Unknown"
        logger.info("➖ ITEM REMOVED FROM CART | Cart ID: ${event.cartId} | Product: $productName")
        logger.info("   └─ User ID: ${event.userId}")
    }

    private fun handleItemQuantityUpdated(event: OrderEvent) {
        val productName = event.details?.get("productName") ?: "Unknown"
        val quantity = event.details?.get("quantity") ?: 0
        logger.info("🔄 ITEM QUANTITY UPDATED | Cart ID: ${event.cartId} | Product: $productName | New Qty: $quantity")
    }

    private fun handleCartCleared(event: OrderEvent) {
        logger.info("🗑️ CART CLEARED | Cart ID: ${event.cartId} | User ID: ${event.userId}")
    }

    private fun handleCartDeleted(event: OrderEvent) {
        logger.info("❌ CART DELETED | Cart ID: ${event.cartId} | User ID: ${event.userId}")
    }

    // Checkout event handlers
    private fun handleCheckoutStarted(event: OrderEvent) {
        logger.info("🛍️ CHECKOUT STARTED | Cart ID: ${event.cartId} | Total: ${event.totalAmount}")
        logger.info("   └─ Item Count: ${event.details?.get("itemCount")}")
    }

    private fun handleCheckoutSessionCreated(event: OrderEvent) {
        val sessionId = event.details?.get("sessionId") ?: "Unknown"
        logger.info("💳 CHECKOUT SESSION CREATED | Cart ID: ${event.cartId} | Session: $sessionId")
        logger.info("   └─ Amount: ${event.totalAmount} | User ID: ${event.userId}")
    }

    // Payment event handlers
    private fun handlePaymentSucceeded(event: OrderEvent) {
        logger.info("✅ PAYMENT SUCCEEDED | Amount: ${event.totalAmount} | User ID: ${event.userId}")
        logger.info("   └─ Details: ${event.details}")
    }

    private fun handlePaymentFailed(event: OrderEvent) {
        val error = event.details?.get("error") ?: "Unknown error"
        logger.warn("❌ PAYMENT FAILED | Amount: ${event.totalAmount} | Error: $error")
        logger.warn("   └─ User ID: ${event.userId}")
    }

    // Order event handlers
    private fun handleOrderCreated(event: OrderEvent) {
        logger.info("📦 ORDER CREATED | Cart ID: ${event.cartId} | Total: ${event.totalAmount}")
        logger.info("   └─ User ID: ${event.userId} | Payment Status: ${event.details?.get("paymentStatus")}")
    }

    private fun handleOrderCompleted(event: OrderEvent) {
        val paymentId = event.details?.get("paymentId") ?: "Unknown"
        logger.info("🎉 ORDER COMPLETED | Cart ID: ${event.cartId} | Payment ID: $paymentId")
        logger.info("   └─ User ID: ${event.userId} | Status: COMPLETED")
    }

    private fun handleOrderConfirmed(event: OrderEvent) {
        logger.info("✅ ORDER CONFIRMED | Order ID: ${event.orderId} | Cart ID: ${event.cartId}")
    }

    private fun handleOrderShipped(event: OrderEvent) {
        logger.info("🚚 ORDER SHIPPED | Order ID: ${event.orderId}")
    }

    private fun sendNotification(event: NotificationEvent) {
        logger.info("📧 SENDING NOTIFICATION | Type: ${event.notificationType} | To: ${event.email}")
        logger.info("   └─ Subject: ${event.subject}")
        logger.info("   └─ Message: ${event.message}")
    }
}

