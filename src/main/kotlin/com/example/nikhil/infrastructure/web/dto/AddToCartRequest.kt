package com.example.nikhil.infrastructure.web.dto
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
/**
 * DTO for adding item to cart request
 * Single Responsibility: Only handles add-to-cart request validation
 */
data class AddToCartRequest(
    @field:NotNull(message = "Product ID is required")
    val productId: Long,
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int = 1
)
