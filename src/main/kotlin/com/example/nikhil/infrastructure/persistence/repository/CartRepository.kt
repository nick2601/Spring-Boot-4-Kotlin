package com.example.nikhil.infrastructure.persistence.repository

import com.example.nikhil.infrastructure.persistence.entity.Cart
import com.example.nikhil.infrastructure.persistence.entity.CartStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repository for Cart entity CRUD operations
 */
@Repository
interface CartRepository : JpaRepository<Cart, Long> {

    /**
     * Find cart by user ID
     */
    fun findByUserId(userId: Long): Optional<Cart>

    /**
     * Find active cart by user ID
     */
    fun findByUserIdAndStatus(userId: Long, status: CartStatus): Optional<Cart>

    /**
     * Check if user has a cart
     */
    fun existsByUserId(userId: Long): Boolean

    /**
     * Find all carts by status
     */
    fun findAllByStatus(status: CartStatus): List<Cart>

    /**
     * Find cart with items eagerly loaded
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.id = :cartId")
    fun findByIdWithItems(cartId: Long): Optional<Cart>

    /**
     * Find user's cart with items eagerly loaded
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId AND c.status = :status")
    fun findByUserIdWithItems(userId: Long, status: CartStatus): Optional<Cart>
}

