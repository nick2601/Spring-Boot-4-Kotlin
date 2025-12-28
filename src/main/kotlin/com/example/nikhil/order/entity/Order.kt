package com.example.nikhil.order.entity

import com.example.nikhil.order.OrderItem
import com.example.nikhil.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime
import java.math.BigDecimal

/**
 * Order entity - represents a completed e-commerce order
 * Created when a cart checkout is completed
 */
@Entity
@Table(name = "orders")
class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @Column(name = "order_number", nullable = false, unique = true)
    var orderNumber: String? = null,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PENDING,

    // ===================== Financial columns (added to match DB migration V10) =====================
    @Column(name = "subtotal", nullable = false)
    var subtotal: BigDecimal = BigDecimal.ZERO,

    @Column(name = "tax", nullable = false)
    var tax: BigDecimal = BigDecimal.ZERO,

    @Column(name = "shipping_cost", nullable = false)
    var shippingCost: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_amount", nullable = false)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    // ==============================================================================================

    @Column(name = "payment_id")
    var paymentId: String? = null,

    @Column(name = "payment_method")
    var paymentMethod: String? = null,

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    var shippingAddress: String? = null,

    @Column(name = "billing_address", columnDefinition = "TEXT")
    var billingAddress: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val items: MutableList<OrderItem> = mutableListOf()
) {
    fun addItem(item: OrderItem) {
        items.add(item)
        item.order = this
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun toString(): String = "Order(id=$id, orderNumber=$orderNumber, status=$status)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Order) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}

/**
 * Order status enum
 */
enum class OrderStatus {
    PENDING,        // Order created, awaiting payment
    PAID,           // Payment received
    PROCESSING,     // Order being prepared
    SHIPPED,        // Order shipped
    DELIVERED,      // Order delivered
    CANCELLED,      // Order cancelled
    REFUNDED        // Order refunded
}
