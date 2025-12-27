package com.example.nikhil.order.dto

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * DTO for Order response
 */
data class OrderDto(
    val id: Long?,
    val orderNumber: String?,
    val userId: Long?,
    val userName: String?,
    val status: String,
    val paymentId: String?,
    val paymentMethod: String?,
    val items: List<OrderItemDto>,
    val itemCount: Int,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

/**
 * DTO for OrderItem response
 */
data class OrderItemDto(
    val id: Long?,
    val productId: Long?,
    val productName: String,
    val productDescription: String?,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val subtotal: BigDecimal
)

/**
 * DTO for creating an order from cart checkout
 */
data class CreateOrderRequest(
    val cartId: Long,
    val paymentMethod: String = "STRIPE"
)

/**
 * DTO for updating order status
 */
data class UpdateOrderStatusRequest(
    val status: String,
    val notes: String? = null
)

/**
 * DTO for order summary (list view)
 */
data class OrderSummaryDto(
    val id: Long?,
    val orderNumber: String?,
    val status: String,
    val itemCount: Int,
    val createdAt: LocalDateTime?
)

/**
 * DTO for order history response
 */
data class OrderHistoryResponse(
    val orders: List<OrderSummaryDto>,
    val totalOrders: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
