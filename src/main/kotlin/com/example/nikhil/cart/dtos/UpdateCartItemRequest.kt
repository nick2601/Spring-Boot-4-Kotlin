package com.example.nikhil.cart.dtos

import jakarta.validation.constraints.Min

/**
 * DTO for updating cart item quantity
 * Single Responsibility: Only handles update quantity request validation
 */
data class UpdateCartItemRequest(
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int
)

