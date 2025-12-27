package com.example.nikhil.common.kafka.producer

import com.example.nikhil.common.kafka.event.NotificationEvent
import com.example.nikhil.common.kafka.event.OrderEvent
import com.example.nikhil.common.kafka.event.UserEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

/**
 * Kafka Producer Service
 * Publishes events to Kafka topics
 * Gracefully handles missing Kafka configuration
 */
@Service
class KafkaProducerService(
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(KafkaProducerService::class.java)

    @Autowired(required = false)
    private var kafkaTemplate: KafkaTemplate<String, String>? = null

    @Value("\${app.kafka.topics.user-events:user-events}")
    private lateinit var userEventsTopic: String

    @Value("\${app.kafka.topics.order-events:order-events}")
    private lateinit var orderEventsTopic: String

    @Value("\${app.kafka.topics.notification-events:notification-events}")
    private lateinit var notificationEventsTopic: String

    private fun isKafkaEnabled(): Boolean = kafkaTemplate != null

    /**
     * Publish user event
     */
    fun publishUserEvent(event: UserEvent) {
        if (!isKafkaEnabled()) {
            logger.debug("Kafka disabled - logging user event: ${event.action} for user ${event.userId}")
            return
        }
        try {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate?.send(userEventsTopic, event.userId.toString(), message)
            logger.info("Published user event: ${event.action} for user ${event.userId}")
        } catch (e: Exception) {
            logger.error("Failed to publish user event: ${e.message}", e)
        }
    }

    /**
     * Publish order event
     */
    fun publishOrderEvent(event: OrderEvent) {
        if (!isKafkaEnabled()) {
            logger.debug("Kafka disabled - logging order event: ${event.eventType} for cart ${event.cartId}")
            return
        }
        try {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate?.send(orderEventsTopic, event.userId.toString(), message)
            logger.info("Published order event: ${event.eventType} for cart ${event.cartId}")
        } catch (e: Exception) {
            logger.error("Failed to publish order event: ${e.message}", e)
        }
    }

    /**
     * Publish notification event
     */
    fun publishNotificationEvent(event: NotificationEvent) {
        if (!isKafkaEnabled()) {
            logger.debug("Kafka disabled - logging notification event: ${event.notificationType} for user ${event.userId}")
            return
        }
        try {
            val message = objectMapper.writeValueAsString(event)
            kafkaTemplate?.send(notificationEventsTopic, event.userId.toString(), message)
            logger.info("Published notification event: ${event.notificationType} for user ${event.userId}")
        } catch (e: Exception) {
            logger.error("Failed to publish notification event: ${e.message}", e)
        }
    }

    /**
     * Publish generic message to a topic
     */
    fun publishMessage(topic: String, key: String, message: String) {
        if (!isKafkaEnabled()) {
            logger.debug("Kafka disabled - logging message to topic: $topic")
            return
        }
        try {
            kafkaTemplate?.send(topic, key, message)
            logger.info("Published message to topic: $topic with key: $key")
        } catch (e: Exception) {
            logger.error("Failed to publish message to topic $topic: ${e.message}", e)
        }
    }
}

