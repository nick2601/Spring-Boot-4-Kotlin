package com.example.nikhil.cart.dtos

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * DTO for CartItem response
 * Single Responsibility: Only represents cart item data for API responses
 */
data class CartItemDto(
    val id: Long? = null,
    val productId: Long? = null,
    val productName: String? = null,
    val productPrice: BigDecimal? = null,
    val quantity: Int = 1,
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val addedAt: LocalDateTime? = null
)

