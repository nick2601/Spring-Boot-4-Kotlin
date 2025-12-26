package com.example.nikhil.infrastructure.persistence.repository

import com.example.nikhil.infrastructure.persistence.entity.CartItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Repository for CartItem entity CRUD operations
 */
@Repository
interface CartItemRepository : JpaRepository<CartItem, Long> {

    /**
     * Find all items in a cart
     */
    fun findAllByCartId(cartId: Long): List<CartItem>

    /**
     * Find specific item in cart by product
     */
    fun findByCartIdAndProductId(cartId: Long, productId: Long): Optional<CartItem>

    /**
     * Check if product exists in cart
     */
    fun existsByCartIdAndProductId(cartId: Long, productId: Long): Boolean

    /**
     * Delete all items in a cart
     */
    fun deleteAllByCartId(cartId: Long)

    /**
     * Delete specific product from cart
     */
    fun deleteByCartIdAndProductId(cartId: Long, productId: Long)

    /**
     * Count items in a cart
     */
    fun countByCartId(cartId: Long): Long

    /**
     * Find item with product details eagerly loaded
     */
    @Query("SELECT ci FROM CartItem ci LEFT JOIN FETCH ci.product WHERE ci.cart.id = :cartId")
    fun findAllByCartIdWithProducts(cartId: Long): List<CartItem>
}

