package com.example.nikhil.infrastructure.mapper

import com.example.nikhil.infrastructure.persistence.entity.Order
import com.example.nikhil.infrastructure.persistence.entity.OrderItem
import com.example.nikhil.infrastructure.web.dto.OrderDto
import com.example.nikhil.infrastructure.web.dto.OrderItemDto
import com.example.nikhil.infrastructure.web.dto.OrderSummaryDto
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
            subtotal = order.subtotal,
            tax = order.tax,
            shippingCost = order.shippingCost,
            totalAmount = order.totalAmount,
            paymentId = order.paymentId,
            paymentMethod = order.paymentMethod,
            shippingAddress = order.shippingAddress,
            billingAddress = order.billingAddress,
            notes = order.notes,
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
            totalAmount = order.totalAmount,
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

