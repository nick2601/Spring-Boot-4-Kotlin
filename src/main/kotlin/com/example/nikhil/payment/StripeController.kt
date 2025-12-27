package com.example.nikhil.payment

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Stripe Payment Controller
 * Handles payment processing with Stripe
 */
@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Stripe payment endpoints")
class StripeController(
    private val stripeService: StripeService
) {
    private val logger = LoggerFactory.getLogger(StripeController::class.java)

    /**
     * Create a Stripe Checkout Session
     */
    @PostMapping("/checkout")
    @Operation(
        summary = "Create checkout session",
        description = "Creates a Stripe checkout session for cart payment"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Checkout session created"),
            ApiResponse(responseCode = "400", description = "Invalid request", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Cart not found", content = [Content()])
        ]
    )
    fun createCheckoutSession(
        @Valid @RequestBody request: CreateCheckoutRequest
    ): ResponseEntity<CheckoutSessionResponse> {
        val response = stripeService.createCheckoutSession(request)
        return ResponseEntity.ok(response)
    }

    /**
     * Create a Payment Intent for custom payment flow
     */
    @PostMapping("/payment-intent")
    @Operation(
        summary = "Create payment intent",
        description = "Creates a Stripe payment intent for custom payment flow"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Payment intent created"),
            ApiResponse(responseCode = "400", description = "Invalid request", content = [Content()])
        ]
    )
    fun createPaymentIntent(
        @Valid @RequestBody request: CreatePaymentIntentRequest
    ): ResponseEntity<PaymentIntentResponse> {
        val response = stripeService.createPaymentIntent(request)
        return ResponseEntity.ok(response)
    }

    /**
     * Get checkout session details
     */
    @GetMapping("/checkout/{sessionId}")
    @Operation(
        summary = "Get checkout session",
        description = "Retrieves details of a checkout session"
    )
    fun getCheckoutSession(@PathVariable sessionId: String): ResponseEntity<Map<String, Any>> {
        val session = stripeService.getCheckoutSession(sessionId)
        return ResponseEntity.ok(
            mapOf(
                "sessionId" to session.id,
                "status" to session.status,
                "paymentStatus" to session.paymentStatus,
                "amountTotal" to session.amountTotal,
                "currency" to session.currency
            )
        )
    }

    /**
     * Payment success callback
     */
    @GetMapping("/success")
    @Operation(summary = "Payment success callback")
    fun paymentSuccess(@RequestParam("session_id") sessionId: String): ResponseEntity<PaymentSuccessResponse> {
        logger.info("Payment success callback for session: $sessionId")
        return ResponseEntity.ok(
            PaymentSuccessResponse(
                success = true,
                message = "Payment completed successfully!",
                paymentId = sessionId
            )
        )
    }

    /**
     * Payment cancel callback
     */
    @GetMapping("/cancel")
    @Operation(summary = "Payment cancel callback")
    fun paymentCancel(): ResponseEntity<PaymentSuccessResponse> {
        logger.info("Payment cancelled by user")
        return ResponseEntity.ok(
            PaymentSuccessResponse(
                success = false,
                message = "Payment was cancelled"
            )
        )
    }

    /**
     * Stripe Webhook endpoint
     * Receives events from Stripe
     */
    @PostMapping("/webhook")
    @Operation(
        summary = "Stripe webhook",
        description = "Receives webhook events from Stripe"
    )
    fun handleWebhook(
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String
    ): ResponseEntity<String> {
        return try {
            val result = stripeService.handleWebhookEvent(payload, signature)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            logger.error("Webhook error: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error: ${e.message}")
        } catch (e: Exception) {
            logger.error("Webhook processing error: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook")
        }
    }
}