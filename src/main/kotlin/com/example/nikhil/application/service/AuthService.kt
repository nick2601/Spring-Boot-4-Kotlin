package com.example.nikhil.application.service

import com.example.nikhil.infrastructure.kafka.event.UserAction
import com.example.nikhil.infrastructure.kafka.event.UserEvent
import com.example.nikhil.infrastructure.kafka.producer.KafkaProducerService
import com.example.nikhil.infrastructure.mapper.UserMapper
import com.example.nikhil.infrastructure.persistence.entity.User
import com.example.nikhil.infrastructure.persistence.repository.UserRepository
import com.example.nikhil.infrastructure.web.dto.AuthRequest
import com.example.nikhil.infrastructure.web.dto.AuthResponse
import com.example.nikhil.infrastructure.web.dto.TokenInfo
import com.example.nikhil.infrastructure.web.dto.TokenPair
import com.example.nikhil.infrastructure.web.dto.UserDto
import com.example.nikhil.infrastructure.web.exception.InvalidCredentialsException
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Authentication Use Case
 * Handles login, token generation, password verification, and authenticated user operations
 *
 * Responsibilities:
 * - User authentication and authorization
 * - JWT token lifecycle management
 * - Authentication event publishing
 * - Current user operations
 *
 * Delegates to:
 * - JwtService: Token generation and parsing
 * - PasswordEncoder: Password verification
 * - UserRepository: User data access
 * - KafkaProducerService: Event publishing
 * - UserMapper: Entity-DTO conversion
 */
@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val kafkaProducerService: KafkaProducerService,
    private val userMapper: UserMapper
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    // Helper to get user roles with fallback
    private fun getUserRoles(user: User): List<String> =
        if (user.roles.isNotEmpty()) user.getRoleNames() else listOf("ROLE_CUSTOMER")

    // Helper to create AuthResponse from user and token pair
    private fun createAuthResponse(user: User, tokenPair: TokenPair): AuthResponse =
        AuthResponse(
            accessToken = tokenPair.accessToken,
            refreshToken = tokenPair.refreshToken,
            email = user.email!!,
            userId = user.id!!,
            name = user.name,
            roles = getUserRoles(user),
            accessTokenExpiresIn = tokenPair.accessTokenExpiresIn,
            refreshTokenExpiresIn = tokenPair.refreshTokenExpiresIn
        )

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

        // Generate both access and refresh tokens
        val tokenPair = jwtService.generateTokenPair(
            user.email!!, user.id!!, user.name, getUserRoles(user)
        )
        logger.info("Login successful for email: ${authRequest.email}, userId: ${user.id}, roles: ${getUserRoles(user)}")

        // Publish login event to Kafka
        try {
            kafkaProducerService.publishUserEvent(
                UserEvent(
                    eventType = "USER_LOGIN",
                    userId = user.id!!,
                    email = user.email!!,
                    action = UserAction.LOGGED_IN,
                    details = mapOf("ipAddress" to "unknown", "userAgent" to "unknown")
                )
            )
        } catch (e: Exception) {
            logger.warn("Failed to publish login event to Kafka: ${e.message}")
        }

        return createAuthResponse(user, tokenPair)
    }

    /**
     * Validate if a token is valid for the given email
     */
    fun validateToken(token: String, email: String): Boolean {
        return jwtService.validateToken(token, email)
    }

    /**
     * Extract email from JWT token
     */
    fun getEmailFromToken(token: String): String? {
        return jwtService.getEmailFromToken(token)
    }

    /**
     * Refresh JWT tokens using a valid refresh token
     * @param refreshToken The refresh token
     * @return New AuthResponse with fresh access and refresh tokens
     * @throws InvalidCredentialsException if refresh token is invalid or expired
     */
    fun refreshTokens(refreshToken: String): AuthResponse {
        logger.info("Attempting to refresh tokens")

        // Extract email from refresh token
        val email = jwtService.getEmailFromToken(refreshToken)
            ?: throw InvalidCredentialsException("Invalid refresh token")

        // Validate it's actually a refresh token and not expired
        if (!jwtService.validateRefreshToken(refreshToken, email)) {
            throw InvalidCredentialsException("Refresh token is invalid or expired")
        }

        val user = userRepository.findByEmail(email)
            ?: throw InvalidCredentialsException("User not found")

        // Generate new token pair
        val tokenPair = jwtService.generateTokenPair(
            user.email!!, user.id!!, user.name, getUserRoles(user)
        )
        logger.info("Tokens refreshed successfully for email: $email, roles: ${getUserRoles(user)}")

        return createAuthResponse(user, tokenPair)
    }

    /**
     * Legacy method - generates only access token (backward compatibility)
     */
    fun refreshToken(email: String): String {
        logger.info("Refreshing token for email: $email")

        val user = userRepository.findByEmail(email)
            ?: throw InvalidCredentialsException("User not found")

        return jwtService.generateAccessToken(
            user.email!!, user.id!!, user.name, getUserRoles(user)
        )
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

    /**
     * Get current authenticated user by email
     * @param email User's email from authentication context
     * @return UserDto representing the current user
     * @throws NoSuchElementException if user not found
     */
    fun getCurrentUser(email: String): UserDto {
        logger.debug("Fetching current user with email: $email")
        val user = userRepository.findByEmail(email)
            ?: throw NoSuchElementException("User not found with email: $email")
        return userMapper.toDto(user)
    }

    /**
     * Get detailed token information
     * Extracts all claims and metadata from JWT token
     * @param token JWT token string
     * @return TokenInfo containing all token claims
     */
    fun getTokenInfo(token: String): TokenInfo {
        return TokenInfo(
            email = jwtService.getEmailFromToken(token),
            userId = jwtService.getUserIdFromToken(token),
            name = jwtService.getNameFromToken(token),
            roles = jwtService.getRolesFromToken(token),
            tokenId = jwtService.getTokenIdFromToken(token),
            issuer = jwtService.getIssuerFromToken(token),
            issuedAt = jwtService.getIssuedAtFromToken(token),
            expiration = jwtService.getExpirationFromToken(token)
        )
    }
}
