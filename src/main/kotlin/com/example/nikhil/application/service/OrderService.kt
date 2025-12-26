package com.example.nikhil.application.service

import com.example.nikhil.infrastructure.kafka.event.OrderAction
import com.example.nikhil.infrastructure.kafka.event.OrderEvent
import com.example.nikhil.infrastructure.kafka.producer.KafkaProducerService
import com.example.nikhil.infrastructure.mapper.OrderMapper
import com.example.nikhil.infrastructure.persistence.entity.*
import com.example.nikhil.infrastructure.persistence.repository.CartRepository
import com.example.nikhil.infrastructure.persistence.repository.OrderRepository
import com.example.nikhil.infrastructure.persistence.repository.UserRepository
import com.example.nikhil.infrastructure.web.dto.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.*

/**
 * Order Service
 * Handles order creation, management, and order history
 */
@Service
@Transactional(readOnly = true)
class OrderService(
    private val orderRepository: OrderRepository,
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    private val orderMapper: OrderMapper,
    private val kafkaProducerService: KafkaProducerService
) {
    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    companion object {
        private const val TAX_RATE = 0.10 // 10% tax rate
        private val FREE_SHIPPING_THRESHOLD = BigDecimal("50.00")
        private val STANDARD_SHIPPING = BigDecimal("5.99")
    }

    // ==================== Order Creation ====================

    /**
     * Create an order from a cart (checkout)
     */
    @Transactional
    fun createOrderFromCart(request: CreateOrderRequest): OrderDto {
        logger.info("Creating order from cart: ${request.cartId}")

        // Get cart with items
        val cart = cartRepository.findByIdWithItems(request.cartId)
            .orElseThrow { NoSuchElementException("Cart not found with id: ${request.cartId}") }

        // Validate cart
        if (cart.items.isEmpty()) {
            throw IllegalStateException("Cannot create order from empty cart")
        }

        if (cart.status == CartStatus.COMPLETED) {
            throw IllegalStateException("Cart has already been checked out")
        }

        val user = cart.user ?: throw IllegalStateException("Cart has no associated user")

        // Calculate totals
        val subtotal = calculateSubtotal(cart.items)
        val shippingCost = calculateShipping(subtotal)
        val tax = calculateTax(subtotal)
        val totalAmount = subtotal.add(shippingCost).add(tax)

        // Create order
        val order = Order(
            user = user,
            orderNumber = generateOrderNumber(),
            status = OrderStatus.PENDING,
            subtotal = subtotal,
            tax = tax,
            shippingCost = shippingCost,
            totalAmount = totalAmount,
            paymentMethod = request.paymentMethod,
            shippingAddress = request.shippingAddress,
            billingAddress = request.billingAddress,
            notes = request.notes
        )

        // Create order items from cart items
        cart.items.forEach { cartItem ->
            val product = cartItem.product ?: return@forEach
            val orderItem = OrderItem(
                order = order,
                product = product,
                productName = product.name ?: "Unknown Product",
                productDescription = product.description,
                unitPrice = product.price ?: BigDecimal.ZERO,
                quantity = cartItem.quantity
            )
            orderItem.calculateSubtotal()
            order.addItem(orderItem)
        }

        // Save order
        val savedOrder = orderRepository.save(order)
        logger.info("Order created: ${savedOrder.orderNumber} for user: ${user.id}")

        // Update cart status
        cart.status = CartStatus.CHECKOUT
        cartRepository.save(cart)

        // Publish order created event
        publishOrderEvent(savedOrder, "ORDER_CREATED", OrderAction.CREATED)

        return orderMapper.toDto(savedOrder)
    }

    /**
     * Complete order after successful payment
     */
    @Transactional
    fun completeOrder(orderId: Long, paymentId: String): OrderDto {
        logger.info("Completing order: $orderId with payment: $paymentId")

        val order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow { NoSuchElementException("Order not found with id: $orderId") }

        order.status = OrderStatus.PAID
        order.paymentId = paymentId
        order.updatedAt = LocalDateTime.now()

        val savedOrder = orderRepository.save(order)

        // Mark the user's cart as completed
        order.user?.id?.let { userId ->
            cartRepository.findByUserIdAndStatus(userId, CartStatus.CHECKOUT)
                .ifPresent { cart ->
                    cart.status = CartStatus.COMPLETED
                    cartRepository.save(cart)
                }
        }

        logger.info("Order completed: ${order.orderNumber}")

        // Publish order completed event
        publishOrderEvent(savedOrder, "ORDER_COMPLETED", OrderAction.CONFIRMED)

        return orderMapper.toDto(savedOrder)
    }

    /**
     * Complete order by order number
     */
    @Transactional
    fun completeOrderByNumber(orderNumber: String, paymentId: String): OrderDto {
        logger.info("Completing order by number: $orderNumber")

        val order = orderRepository.findByOrderNumberWithItems(orderNumber)
            .orElseThrow { NoSuchElementException("Order not found with number: $orderNumber") }

        return completeOrder(order.id!!, paymentId)
    }

    // ==================== Order Retrieval ====================

    /**
     * Get order by ID
     */
    fun getOrderById(orderId: Long): OrderDto {
        logger.debug("Fetching order: $orderId")
        val order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow { NoSuchElementException("Order not found with id: $orderId") }
        return orderMapper.toDto(order)
    }

    /**
     * Get order by order number
     */
    fun getOrderByNumber(orderNumber: String): OrderDto {
        logger.debug("Fetching order by number: $orderNumber")
        val order = orderRepository.findByOrderNumberWithItems(orderNumber)
            .orElseThrow { NoSuchElementException("Order not found with number: $orderNumber") }
        return orderMapper.toDto(order)
    }

    /**
     * Get order history for a user
     */
    fun getOrderHistory(userId: Long, page: Int = 0, size: Int = 10): OrderHistoryResponse {
        logger.debug("Fetching order history for user: $userId, page: $page, size: $size")

        val pageable = PageRequest.of(page, size)
        val ordersPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)

        val summaries = ordersPage.content.map { orderMapper.toSummaryDto(it) }

        return OrderHistoryResponse(
            orders = summaries,
            totalOrders = ordersPage.totalElements,
            page = page,
            pageSize = size,
            totalPages = ordersPage.totalPages
        )
    }

    /**
     * Get all orders for a user (no pagination)
     */
    fun getAllOrdersForUser(userId: Long): List<OrderSummaryDto> {
        logger.debug("Fetching all orders for user: $userId")
        val orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
        return orderMapper.toSummaryDtoList(orders)
    }

    /**
     * Get orders by status for a user
     */
    fun getOrdersByStatus(userId: Long, status: OrderStatus): List<OrderSummaryDto> {
        logger.debug("Fetching orders for user: $userId with status: $status")
        val orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
        return orderMapper.toSummaryDtoList(orders)
    }

    // ==================== Order Status Management ====================

    /**
     * Update order status
     */
    @Transactional
    fun updateOrderStatus(orderId: Long, request: UpdateOrderStatusRequest): OrderDto {
        logger.info("Updating order $orderId status to ${request.status}")

        val order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow { NoSuchElementException("Order not found with id: $orderId") }

        val newStatus = try {
            OrderStatus.valueOf(request.status.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid order status: ${request.status}")
        }

        order.status = newStatus
        request.notes?.let { order.notes = it }
        order.updatedAt = LocalDateTime.now()

        val savedOrder = orderRepository.save(order)
        logger.info("Order $orderId status updated to $newStatus")

        // Publish status update event
        val action = when (newStatus) {
            OrderStatus.SHIPPED -> OrderAction.SHIPPED
            OrderStatus.DELIVERED -> OrderAction.DELIVERED
            OrderStatus.CANCELLED -> OrderAction.CANCELLED
            else -> OrderAction.UPDATED
        }
        publishOrderEvent(savedOrder, "ORDER_STATUS_UPDATED", action)

        return orderMapper.toDto(savedOrder)
    }

    /**
     * Cancel order
     */
    @Transactional
    fun cancelOrder(orderId: Long, reason: String? = null): OrderDto {
        logger.info("Cancelling order: $orderId")

        val order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow { NoSuchElementException("Order not found with id: $orderId") }

        if (order.status in listOf(OrderStatus.SHIPPED, OrderStatus.DELIVERED)) {
            throw IllegalStateException("Cannot cancel order that has been shipped or delivered")
        }

        order.status = OrderStatus.CANCELLED
        order.notes = reason ?: order.notes
        order.updatedAt = LocalDateTime.now()

        val savedOrder = orderRepository.save(order)
        logger.info("Order cancelled: ${order.orderNumber}")

        // Publish order cancelled event
        publishOrderEvent(savedOrder, "ORDER_CANCELLED", OrderAction.CANCELLED)

        return orderMapper.toDto(savedOrder)
    }

    // ==================== Private Helper Methods ====================

    private fun generateOrderNumber(): String {
        val timestamp = System.currentTimeMillis()
        val random = (1000..9999).random()
        return "ORD-$timestamp-$random"
    }

    private fun calculateSubtotal(items: List<CartItem>): BigDecimal {
        return items.sumOf { item ->
            val price = item.product?.price ?: BigDecimal.ZERO
            price.multiply(BigDecimal(item.quantity))
        }
    }

    private fun calculateShipping(subtotal: BigDecimal): BigDecimal {
        return if (subtotal >= FREE_SHIPPING_THRESHOLD) BigDecimal.ZERO else STANDARD_SHIPPING
    }

    private fun calculateTax(subtotal: BigDecimal): BigDecimal {
        return subtotal.multiply(BigDecimal(TAX_RATE)).setScale(2, RoundingMode.HALF_UP)
    }

    private fun publishOrderEvent(order: Order, eventType: String, action: OrderAction) {
        try {
            kafkaProducerService.publishOrderEvent(
                OrderEvent(
                    eventType = eventType,
                    userId = order.user?.id ?: 0L,
                    cartId = null,
                    orderId = order.id,
                    orderNumber = order.orderNumber,
                    action = action,
                    totalAmount = order.totalAmount,
                    details = mapOf(
                        "orderNumber" to (order.orderNumber ?: ""),
                        "status" to order.status.name,
                        "itemCount" to order.items.size
                    )
                )
            )
            logger.debug("Published $eventType event for order: ${order.orderNumber}")
        } catch (e: Exception) {
            logger.warn("Failed to publish $eventType event: ${e.message}")
        }
    }
}

