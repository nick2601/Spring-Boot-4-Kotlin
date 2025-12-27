package com.example.nikhil.order.mapper

import com.example.nikhil.order.OrderItem
import com.example.nikhil.order.dto.OrderDto
import com.example.nikhil.order.dto.OrderItemDto
import com.example.nikhil.order.dto.OrderSummaryDto
import com.example.nikhil.order.entity.Order
import org.springframework.stereotype.Component

/**
 * Order Mapper
 * Handles conversion between Order entities and DTOs
 */
@Component
class OrderMapper {

    /**
     * Convert Order entity to OrderDto
     */
    fun toDto(order: Order): OrderDto {
        val items = order.items.map { toItemDto(it) }

        return OrderDto(
            id = order.id,
            orderNumber = order.orderNumber,
            userId = order.user?.id,
            userName = order.user?.name,
            status = order.status.name,
            paymentId = order.paymentId,
            paymentMethod = order.paymentMethod,
            items = items,
            itemCount = items.sumOf { it.quantity },
            createdAt = order.createdAt,
            updatedAt = order.updatedAt
        )
    }

    /**
     * Convert OrderItem entity to OrderItemDto
     */
    fun toItemDto(item: OrderItem): OrderItemDto {
        return OrderItemDto(
            id = item.id,
            productId = item.product?.id,
            productName = item.productName,
            productDescription = item.productDescription,
            unitPrice = item.unitPrice,
            quantity = item.quantity,
            subtotal = item.subtotal
        )
    }

    /**
     * Convert Order entity to OrderSummaryDto (for list views)
     */
    fun toSummaryDto(order: Order): OrderSummaryDto {
        return OrderSummaryDto(
            id = order.id,
            orderNumber = order.orderNumber,
            status = order.status.name,
            itemCount = order.items.sumOf { it.quantity },
            createdAt = order.createdAt
        )
    }

    /**
     * Convert list of orders to summary DTOs
     */
    fun toSummaryDtoList(orders: List<Order>): List<OrderSummaryDto> {
        return orders.map { toSummaryDto(it) }
    }
}
