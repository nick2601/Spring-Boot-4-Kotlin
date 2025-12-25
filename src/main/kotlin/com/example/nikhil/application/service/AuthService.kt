package com.example.nikhil.application.service

import com.example.nikhil.infrastructure.kafka.event.UserAction
import com.example.nikhil.infrastructure.kafka.event.UserEvent
import com.example.nikhil.infrastructure.kafka.producer.KafkaProducerService
import com.example.nikhil.infrastructure.persistence.repository.UserRepository
import com.example.nikhil.infrastructure.security.JwtTokenUtil
import com.example.nikhil.infrastructure.web.dto.AuthRequest
import com.example.nikhil.infrastructure.web.dto.AuthResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Authentication Use Case
 * Handles login, token generation, and password verification
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenUtil: JwtTokenUtil,
    private val kafkaProducerService: KafkaProducerService
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    /**
     * Authenticate user and generate JWT token
     * @param authRequest contains email and password
     * @return AuthResponse with JWT token
     * @throws InvalidCredentialsException if authentication fails
     */
    fun login(authRequest: AuthRequest): AuthResponse {
        logger.debug("Attempting login for email: ${authRequest.email}")

        val user = userRepository.findByEmail(authRequest.email)
            ?: run {
                logger.warn("Login failed: User not found for email: ${authRequest.email}")
                throw InvalidCredentialsException("Invalid email or password")
            }

        if (!passwordEncoder.matches(authRequest.password, user.password)) {
            logger.warn("Login failed: Invalid password for email: ${authRequest.email}")
            throw InvalidCredentialsException("Invalid email or password")
        }

        val email = user.email ?: throw InvalidCredentialsException("User email is null")
        val token = jwtTokenUtil.generateToken(email)
        logger.info("Login successful for email: ${authRequest.email}")

        // Publish login event to Kafka
        try {
            kafkaProducerService.publishUserEvent(
                UserEvent(
                    eventType = "USER_LOGIN",
                    userId = user.id!!,
                    email = email,
                    action = UserAction.LOGGED_IN,
                    details = mapOf("ipAddress" to "unknown", "userAgent" to "unknown")
                )
            )
        } catch (e: Exception) {
            logger.warn("Failed to publish login event to Kafka: ${e.message}")
        }

        return AuthResponse(
            token = token,
            email = email,
            name = user.name
        )
    }

    /**
     * Validate if a token is valid for the given email
     */
    fun validateToken(token: String, email: String): Boolean {
        return jwtTokenUtil.validateToken(token, email)
    }

    /**
     * Extract email from JWT token
     */
    fun getEmailFromToken(token: String): String? {
        return jwtTokenUtil.getEmailFromToken(token)
    }

    /**
     * Refresh JWT token - generates a new token for the user
     */
    fun refreshToken(email: String): String {
        logger.info("Refreshing token for email: $email")
        return jwtTokenUtil.generateToken(email)
    }

    /**
     * Publish logout event to Kafka
     */
    fun publishLogoutEvent(email: String) {
        val user = userRepository.findByEmail(email)
        user?.let {
            try {
                kafkaProducerService.publishUserEvent(
                    UserEvent(
                        eventType = "USER_LOGOUT",
                        userId = it.id!!,
                        email = email,
                        action = UserAction.LOGGED_OUT,
                        details = null
                    )
                )
                logger.info("Published logout event for user: $email")
            } catch (e: Exception) {
                logger.warn("Failed to publish logout event to Kafka: ${e.message}")
            }
        }
    }
}

/**
 * Exception thrown when authentication fails
 */
class InvalidCredentialsException(message: String) : RuntimeException(message)

