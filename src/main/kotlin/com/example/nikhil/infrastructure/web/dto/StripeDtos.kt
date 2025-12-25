package com.example.nikhil.infrastructure.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

/**
 * Request to create a Stripe checkout session
 */
@Schema(description = "Create checkout session request")
data class CreateCheckoutRequest(
    @Schema(description = "Cart ID to checkout", example = "1", required = true)
    @field:NotNull(message = "Cart ID is required")
    val cartId: Long,

    @Schema(description = "User ID", example = "1", required = true)
    @field:NotNull(message = "User ID is required")
    val userId: Long,

    @Schema(description = "Currency code", example = "usd")
    val currency: String = "usd"
)

/**
 * Response containing checkout session URL
 */
@Schema(description = "Checkout session response")
data class CheckoutSessionResponse(
    @Schema(description = "Stripe session ID")
    val sessionId: String,

    @Schema(description = "Checkout URL to redirect user")
    val checkoutUrl: String,

    @Schema(description = "Total amount in cents")
    val amount: Long
)

/**
 * Request to create a payment intent
 */
@Schema(description = "Create payment intent request")
data class CreatePaymentIntentRequest(
    @Schema(description = "Amount in cents", example = "5000", required = true)
    @field:NotNull(message = "Amount is required")
    @field:Min(value = 50, message = "Minimum amount is 50 cents")
    val amount: Long,

    @Schema(description = "Currency code", example = "usd")
    val currency: String = "usd",

    @Schema(description = "User ID", example = "1")
    val userId: Long? = null,

    @Schema(description = "Description of the payment")
    val description: String? = null
)

/**
 * Response containing payment intent details
 */
@Schema(description = "Payment intent response")
data class PaymentIntentResponse(
    @Schema(description = "Payment intent ID")
    val paymentIntentId: String,

    @Schema(description = "Client secret for frontend")
    val clientSecret: String,

    @Schema(description = "Amount in cents")
    val amount: Long,

    @Schema(description = "Currency")
    val currency: String,

    @Schema(description = "Payment status")
    val status: String
)

/**
 * Payment success response
 */
@Schema(description = "Payment success response")
data class PaymentSuccessResponse(
    @Schema(description = "Payment was successful")
    val success: Boolean = true,

    @Schema(description = "Message")
    val message: String,

    @Schema(description = "Session or Payment Intent ID")
    val paymentId: String? = null
)

