package com.example.nikhil.cart.entity

import com.example.nikhil.user.entity.User
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Cart entity - represents a user's shopping cart
 * Relationship: One user has one cart (OneToOne)
 * Relationship: One cart has many cart items (OneToMany)
 */
@Entity
@Table(name = "carts")
class Cart(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @field:NotNull(message = "User is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: User? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: CartStatus = CartStatus.ACTIVE,

    // OneToMany: One cart has many items
    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val items: MutableList<CartItem> = mutableListOf()
) {
    /**
     * Add item to cart
     */
    fun addItem(item: CartItem) {
        items.add(item)
        item.cart = this
        updatedAt = LocalDateTime.now()
    }

    /**
     * Remove item from cart
     */
    fun removeItem(item: CartItem) {
        items.remove(item)
        item.cart = null
        updatedAt = LocalDateTime.now()
    }

    /**
     * Clear all items from cart
     */
    fun clearItems() {
        items.forEach { it.cart = null }
        items.clear()
        updatedAt = LocalDateTime.now()
    }

    /**
     * Get total number of items in cart
     */
    fun getTotalItems(): Int = items.sumOf { it.quantity }

    /**
     * Get total price of cart
     */
    fun getTotalPrice(): BigDecimal =
        items.mapNotNull { it.product?.price?.multiply(BigDecimal(it.quantity)) }
            .fold(BigDecimal.ZERO) { acc, price -> acc.add(price) }

    override fun toString(): String = "Cart(id=$id, userId=${user?.id}, status=$status, items=${items.size})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cart) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}

/**
 * Cart status enum
 */
enum class CartStatus {
    ACTIVE,      // Currently being used
    CHECKOUT,    // In checkout process
    COMPLETED,   // Order placed
    ABANDONED    // User abandoned cart
}

