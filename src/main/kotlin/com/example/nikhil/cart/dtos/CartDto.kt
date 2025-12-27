package com.example.nikhil.cart.dtos

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * DTO for Cart response
 * Single Responsibility: Only represents cart data for API responses
 */
data class CartDto(
    val id: Long? = null,
    val userId: Long? = null,
    val status: String? = null,
    val items: List<CartItemDto> = emptyList(),
    val totalItems: Int = 0,
    val totalPrice: BigDecimal = BigDecimal.ZERO,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)