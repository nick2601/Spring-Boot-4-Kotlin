package com.example.nikhil.infrastructure.persistence.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * OrderItem entity - represents an item in an order
 * Stores a snapshot of product details at time of purchase
 */
@Entity
@Table(name = "order_items")
class OrderItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null,

    // Snapshot of product details at time of purchase
    @Column(name = "product_name", nullable = false)
    var productName: String = "",

    @Column(name = "product_description", columnDefinition = "TEXT")
    var productDescription: String? = null,

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    var unitPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "quantity", nullable = false)
    var quantity: Int = 1,

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    var subtotal: BigDecimal = BigDecimal.ZERO,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Calculate subtotal based on unit price and quantity
     */
    fun calculateSubtotal(): BigDecimal {
        subtotal = unitPrice.multiply(BigDecimal(quantity))
        return subtotal
    }

    override fun toString(): String = "OrderItem(id=$id, productName=$productName, quantity=$quantity, subtotal=$subtotal)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OrderItem) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}

