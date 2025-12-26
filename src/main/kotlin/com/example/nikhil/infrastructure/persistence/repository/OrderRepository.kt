package com.example.nikhil.infrastructure.persistence.repository

import com.example.nikhil.infrastructure.persistence.entity.Order
import com.example.nikhil.infrastructure.persistence.entity.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    /**
     * Find order by order number
     */
    fun findByOrderNumber(orderNumber: String): Optional<Order>

    /**
     * Find all orders for a user
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Order>

    /**
     * Find all orders for a user with pagination
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<Order>

    /**
     * Find orders by user and status
     */
    fun findByUserIdAndStatusOrderByCreatedAtDesc(userId: Long, status: OrderStatus): List<Order>

    /**
     * Find order by ID with items
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    fun findByIdWithItems(@Param("orderId") orderId: Long): Optional<Order>

    /**
     * Find order by order number with items
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderNumber = :orderNumber")
    fun findByOrderNumberWithItems(@Param("orderNumber") orderNumber: String): Optional<Order>

    /**
     * Find orders by status
     */
    fun findByStatusOrderByCreatedAtDesc(status: OrderStatus): List<Order>

    /**
     * Find orders created between dates
     */
    fun findByCreatedAtBetweenOrderByCreatedAtDesc(startDate: LocalDateTime, endDate: LocalDateTime): List<Order>

    /**
     * Find orders by payment ID
     */
    fun findByPaymentId(paymentId: String): Optional<Order>

    /**
     * Count orders by user
     */
    fun countByUserId(userId: Long): Long

    /**
     * Count orders by status
     */
    fun countByStatus(status: OrderStatus): Long
}

