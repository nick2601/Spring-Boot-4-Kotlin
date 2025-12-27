package com.example.nikhil.common.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

/**
 * Kafka Configuration
 * Configures Kafka topics and settings
 * Only enabled when kafka is available
 */
@Configuration
@ConditionalOnProperty(name = ["spring.kafka.enabled"], havingValue = "true", matchIfMissing = false)
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers:localhost:9092}")
    private lateinit var bootstrapServers: String

    @Value("\${app.kafka.topics.user-events:user-events}")
    private lateinit var userEventsTopic: String

    @Value("\${app.kafka.topics.order-events:order-events}")
    private lateinit var orderEventsTopic: String

    @Value("\${app.kafka.topics.notification-events:notification-events}")
    private lateinit var notificationEventsTopic: String

    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun userEventsTopic(): NewTopic {
        return TopicBuilder.name(userEventsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun orderEventsTopic(): NewTopic {
        return TopicBuilder.name(orderEventsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }

    @Bean
    fun notificationEventsTopic(): NewTopic {
        return TopicBuilder.name(notificationEventsTopic)
            .partitions(3)
            .replicas(1)
            .build()
    }
}