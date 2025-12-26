package com.example.nikhil.infrastructure.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * JWT Configuration Properties
 * Centralized configuration for JWT token management
 *
 * Properties can be configured in application.properties:
 * - jwt.secret
 * - jwt.access-token.expiration
 * - jwt.refresh-token.expiration
 * - jwt.issuer
 * - jwt.auto-refresh.enabled
 * - jwt.auto-refresh.threshold
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
class JwtConfig {

    /**
     * Secret key for signing JWT tokens
     * IMPORTANT: Change this in production!
     */
    var secret: String = "MySecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLongForHS256Algorithm"

    /**
     * Token issuer identifier
     */
    var issuer: String = "learning-spring-rest-api"

    /**
     * Access token configuration
     */
    var accessToken: TokenConfig = TokenConfig(
        expiration = 900000L,  // 15 minutes in milliseconds
        name = "access"
    )

    /**
     * Refresh token configuration
     */
    var refreshToken: TokenConfig = TokenConfig(
        expiration = 604800000L,  // 7 days in milliseconds
        name = "refresh"
    )

    /**
     * Auto-refresh configuration
     */
    var autoRefresh: AutoRefreshConfig = AutoRefreshConfig()

    /**
     * Get access token expiration in seconds
     */
    fun getAccessTokenExpirationSeconds(): Long = accessToken.expiration / 1000

    /**
     * Get refresh token expiration in seconds
     */
    fun getRefreshTokenExpirationSeconds(): Long = refreshToken.expiration / 1000

    /**
     * Check if auto-refresh is enabled
     */
    fun isAutoRefreshEnabled(): Boolean = autoRefresh.enabled

    /**
     * Get auto-refresh threshold in milliseconds
     */
    fun getAutoRefreshThresholdMs(): Long = autoRefresh.threshold
}

/**
 * Token-specific configuration
 */
class TokenConfig(
    /**
     * Token expiration time in milliseconds
     */
    var expiration: Long = 900000L,

    /**
     * Token type name
     */
    var name: String = "access"
)

/**
 * Auto-refresh configuration
 */
class AutoRefreshConfig(
    /**
     * Enable automatic token refresh
     */
    var enabled: Boolean = true,

    /**
     * Threshold before expiry to trigger auto-refresh (in milliseconds)
     * Default: 5 minutes (300000ms)
     * If token expires in less than this time, it will be auto-refreshed
     */
    var threshold: Long = 300000L
)

