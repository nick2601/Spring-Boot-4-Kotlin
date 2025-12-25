package com.example.nikhil.infrastructure.persistence.entity

import jakarta.persistence.*
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

/**
 * CartItem entity - represents an item in a shopping cart
 * Relationship: Many items belong to one cart (ManyToOne)
 * Relationship: Many items can reference one product (ManyToOne)
 */
@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = [UniqueConstraint(columnNames = ["cart_id", "product_id"])]
)
class CartItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    // ManyToOne: Many items belong to one cart
    @field:NotNull(message = "Cart.kt is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: Cart? = null,

    // ManyToOne: Many items can reference one product
    @field:NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null,

    @field:Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    var quantity: Int = 1,

    @Column(name = "added_at", nullable = false, updatable = false)
    var addedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Increase quantity by given amount
     */
    fun increaseQuantity(amount: Int = 1) {
        quantity += amount
    }

    /**
     * Decrease quantity by given amount
     * @return true if quantity is still positive, false if should be removed
     */
    fun decreaseQuantity(amount: Int = 1): Boolean {
        quantity -= amount
        return quantity > 0
    }

    /**
     * Get subtotal for this item
     */
    fun getSubtotal(): java.math.BigDecimal =
        product?.price?.multiply(java.math.BigDecimal(quantity)) ?: java.math.BigDecimal.ZERO

    override fun toString(): String = "CartItem(id=$id, productId=${product?.id}, quantity=$quantity)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CartItem) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}

