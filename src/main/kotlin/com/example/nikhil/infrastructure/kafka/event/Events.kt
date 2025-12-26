package com.example.nikhil.infrastructure.kafka.event

import java.time.LocalDateTime

/**
 * Base event class for Kafka messages
 */
abstract class BaseEvent(
    open val eventId: String = java.util.UUID.randomUUID().toString(),
    open val eventType: String,
    open val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * User-related events
 */
data class UserEvent(
    override val eventId: String = java.util.UUID.randomUUID().toString(),
    override val eventType: String,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val userId: Long,
    val email: String,
    val action: UserAction,
    val details: Map<String, Any>? = null
) : BaseEvent(eventId, eventType, timestamp)

enum class UserAction {
    REGISTERED,
    LOGGED_IN,
    LOGGED_OUT,
    PASSWORD_CHANGED,
    PROFILE_UPDATED,
    DELETED
}

/**
 * Order-related events
 */
data class OrderEvent(
    override val eventId: String = java.util.UUID.randomUUID().toString(),
    override val eventType: String = "ORDER_EVENT",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val orderId: Long? = null,
    val orderNumber: String? = null,
    val userId: Long,
    val cartId: Long? = null,
    val action: OrderAction,
    val totalAmount: java.math.BigDecimal? = null,
    val details: Map<String, Any>? = null
) : BaseEvent(eventId, eventType, timestamp)

enum class OrderAction {
    CREATED,
    UPDATED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED,
    // Cart-specific actions
    CART_CREATED,
    ITEM_ADDED,
    ITEM_REMOVED,
    ITEM_UPDATED,
    CART_CLEARED,
    // Payment-specific actions
    CHECKOUT_STARTED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED
}

/**
 * Notification events
 */
data class NotificationEvent(
    override val eventId: String = java.util.UUID.randomUUID().toString(),
    override val eventType: String = "NOTIFICATION_EVENT",
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val userId: Long,
    val email: String,
    val notificationType: NotificationType,
    val subject: String,
    val message: String,
    val metadata: Map<String, Any>? = null
) : BaseEvent(eventId, eventType, timestamp)

enum class NotificationType {
    EMAIL,
    SMS,
    PUSH,
    IN_APP
}

