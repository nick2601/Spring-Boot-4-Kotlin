package com.example.nikhil.infrastructure.persistence.repository

import com.example.nikhil.infrastructure.persistence.entity.OrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderItemRepository : JpaRepository<OrderItem, Long> {

    /**
     * Find all items for an order
     */
    fun findByOrderId(orderId: Long): List<OrderItem>

    /**
     * Delete all items for an order
     */
    fun deleteAllByOrderId(orderId: Long)
}

