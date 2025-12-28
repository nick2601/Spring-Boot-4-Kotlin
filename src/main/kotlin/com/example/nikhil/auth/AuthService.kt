package com.example.nikhil.auth

import com.example.nikhil.auth.dto.AuthRequest
import com.example.nikhil.auth.dto.AuthResponse
import com.example.nikhil.auth.dto.ChangePasswordRequest
import com.example.nikhil.auth.dto.RegisterUserRequest
import com.example.nikhil.auth.dto.TokenInfo
import com.example.nikhil.auth.dto.TokenPair
import com.example.nikhil.auth.dto.TokenValidationResponse
import com.example.nikhil.common.kafka.event.UserAction
import com.example.nikhil.common.kafka.event.UserEvent
import com.example.nikhil.common.kafka.producer.KafkaProducerService
import com.example.nikhil.user.UserService
import com.example.nikhil.user.dto.UserDto
import com.example.nikhil.user.entity.User
import com.example.nikhil.common.exception.InvalidCredentialsException
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Authentication Use Case
 * Handles login, token generation, and authentication-related operations
 */
@Service
class AuthService(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val kafkaProducerService: KafkaProducerService
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    private fun getUserRoles(user: User): List<String> =
        if (user.roles.isNotEmpty()) user.getRoleNames() else listOf("ROLE_CUSTOMER")

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

    fun login(authRequest: AuthRequest): AuthResponse {
        logger.debug("Attempting login for email: ${authRequest.email}")

        val user = userService.getUserByEmail(authRequest.email)
            ?: throw InvalidCredentialsException("Invalid email or password")

        if (!passwordEncoder.matches(authRequest.password, user.password)) {
            throw InvalidCredentialsException("Invalid email or password")
        }

        val tokenPair = jwtService.generateTokenPair(
            user.email, user.id, user.name, getUserRoles(user)
        )

        publishEvent(user, "USER_LOGIN", UserAction.LOGGED_IN, mapOf("ipAddress" to "unknown"))

        return createAuthResponse(user, tokenPair)
    }

    fun refreshTokens(refreshToken: String): AuthResponse {
        val email = jwtService.getEmailFromToken(refreshToken)
            ?: throw InvalidCredentialsException("Invalid refresh token")

        if (!jwtService.validateRefreshToken(refreshToken, email)) {
            throw InvalidCredentialsException("Refresh token is invalid or expired")
        }

        val user = userService.getUserByEmail(email)
            ?: throw InvalidCredentialsException("User not found")

        val tokenPair = jwtService.generateTokenPair(
            user.email!!, user.id!!, user.name, getUserRoles(user)
        )

        return createAuthResponse(user, tokenPair)
    }

    fun registerUser(request: RegisterUserRequest): UserDto = userService.registerUser(request)

    fun changePassword(request: ChangePasswordRequest) = userService.changePassword(request)

    fun getCurrentUser(email: String): UserDto = userService.getUserDtoByEmail(email)

    fun validateToken(token: String, email: String) = jwtService.validateToken(token, email)

    fun getEmailFromToken(token: String) = jwtService.getEmailFromToken(token)

    fun getTokenInfo(token: String): TokenInfo {
        return TokenInfo(
            email = jwtService.getEmailFromToken(token),
            userId = jwtService.getUserIdFromToken(token),
            name = jwtService.getNameFromToken(token),
            roles = jwtService.getRolesFromToken(token),
            tokenId = null,
            issuer = null,
            issuedAt = null,
            expiration = jwtService.getExpirationFromToken(token)
        )
    }

    fun validateTokenDetailed(token: String): TokenValidationResponse {
        val email = jwtService.getEmailFromToken(token)
        if (email != null && jwtService.validateToken(token, email)) {
            return TokenValidationResponse(
                valid = true,
                message = "Token is valid",
                email = email,
                userId = jwtService.getUserIdFromToken(token),
                name = jwtService.getNameFromToken(token),
                roles = jwtService.getRolesFromToken(token),
                expiresAt = jwtService.getExpirationFromToken(token)?.toString(),
                tokenType = jwtService.getTokenType(token) ?: "unknown",
                timeUntilExpirySeconds = jwtService.getTimeUntilExpirySeconds(token),
                needsRefresh = jwtService.needsRefresh(token)
            )
        }
        return TokenValidationResponse(valid = false, message = "Token is invalid or expired")
    }

    fun publishLogoutEvent(email: String) {
        userService.getUserByEmail(email)?.let {
            publishEvent(it, "USER_LOGOUT", UserAction.LOGGED_OUT)
        }
    }

    private fun publishEvent(user: User, type: String, action: UserAction, details: Map<String, String>? = null) {
        try {
            kafkaProducerService.publishUserEvent(
                UserEvent(
                    eventType = type,
                    userId = user.id!!,
                    email = user.email!!,
                    action = action,
                    details = details
                )
            )
        } catch (e: Exception) {
            logger.warn("Failed to publish $type event: ${e.message}")
        }
    }
}
