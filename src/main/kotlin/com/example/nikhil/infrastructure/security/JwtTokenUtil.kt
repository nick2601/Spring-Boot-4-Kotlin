package com.example.nikhil.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT Token Utility
 * Handles token generation, validation, and parsing
 * Supports both Access Tokens (short-lived) and Refresh Tokens (long-lived)
 *
 * Uses JwtConfig for centralized configuration management
 */
@Component
class JwtTokenUtil(
    private val jwtConfig: JwtConfig
) {
    private val logger = LoggerFactory.getLogger(JwtTokenUtil::class.java)

    companion object {
        const val TOKEN_TYPE_ACCESS = "access"
        const val TOKEN_TYPE_REFRESH = "refresh"
    }

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * Generate Access Token for the given email (backward compatible)
     */
    fun generateToken(email: String): String {
        return generateAccessToken(email, null, null, emptyList())
    }

    /**
     * Generate Access Token with enhanced claims
     * @param email User's email (subject)
     * @param userId User's ID
     * @param name User's name
     * @param roles User's roles/authorities
     */
    fun generateToken(email: String, userId: Long?, name: String?, roles: List<String>): String {
        return generateAccessToken(email, userId, name, roles)
    }

    /**
     * Generate Access Token (short-lived, for API requests)
     */
    fun generateAccessToken(email: String, userId: Long?, name: String?, roles: List<String>): String {
        return generateTokenInternal(email, userId, name, roles, TOKEN_TYPE_ACCESS, jwtConfig.accessToken.expiration)
    }

    /**
     * Generate Refresh Token (long-lived, for getting new access tokens)
     */
    fun generateRefreshToken(email: String, userId: Long?): String {
        return generateTokenInternal(
            email,
            userId,
            null,
            emptyList(),
            TOKEN_TYPE_REFRESH,
            jwtConfig.refreshToken.expiration
        )
    }

    /**
     * Generate both Access and Refresh tokens
     */
    fun generateTokenPair(email: String, userId: Long?, name: String?, roles: List<String>): TokenPair {
        val accessToken = generateAccessToken(email, userId, name, roles)
        val refreshToken = generateRefreshToken(email, userId)
        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtConfig.getAccessTokenExpirationSeconds(),
            refreshTokenExpiresIn = jwtConfig.getRefreshTokenExpirationSeconds()
        )
    }

    /**
     * Internal method to generate tokens
     */
    private fun generateTokenInternal(
        email: String,
        userId: Long?,
        name: String?,
        roles: List<String>,
        tokenType: String,
        expirationTime: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationTime)
        val tokenId = UUID.randomUUID().toString()

        val builder = Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(expiryDate)
            .id(tokenId)
            .issuer(jwtConfig.issuer)
            .claim("type", tokenType) // Distinguish access vs refresh token
            .signWith(secretKey)

        // Add custom claims if available (mainly for access tokens)
        userId?.let { builder.claim("userId", it) }
        name?.let { builder.claim("name", it) }
        if (roles.isNotEmpty()) {
            builder.claim("roles", roles)
        }

        return builder.compact()
    }

    /**
     * Extract email from token
     */
    fun getEmailFromToken(token: String): String? {
        return try {
            getClaims(token)?.subject
        } catch (e: Exception) {
            logger.debug("Failed to extract email from token: ${e.message}")
            null
        }
    }

    /**
     * Extract user ID from token
     */
    fun getUserIdFromToken(token: String): Long? {
        return try {
            getClaims(token)?.get("userId", Number::class.java)?.toLong()
        } catch (e: Exception) {
            logger.debug("Failed to extract userId from token: ${e.message}")
            null
        }
    }

    /**
     * Extract user name from token
     */
    fun getNameFromToken(token: String): String? {
        return try {
            getClaims(token)?.get("name", String::class.java)
        } catch (e: Exception) {
            logger.debug("Failed to extract name from token: ${e.message}")
            null
        }
    }

    /**
     * Extract roles from token
     */
    @Suppress("UNCHECKED_CAST")
    fun getRolesFromToken(token: String): List<String> {
        return try {
            getClaims(token)?.get("roles", List::class.java) as? List<String> ?: emptyList()
        } catch (e: Exception) {
            logger.debug("Failed to extract roles from token: ${e.message}")
            emptyList()
        }
    }

    /**
     * Extract token ID (jti) from token
     */
    fun getTokenIdFromToken(token: String): String? {
        return try {
            getClaims(token)?.id
        } catch (e: Exception) {
            logger.debug("Failed to extract token ID from token: ${e.message}")
            null
        }
    }

    /**
     * Extract issuer from token
     */
    fun getIssuerFromToken(token: String): String? {
        return try {
            getClaims(token)?.issuer
        } catch (e: Exception) {
            logger.debug("Failed to extract issuer from token: ${e.message}")
            null
        }
    }

    /**
     * Extract issued at time from token
     */
    fun getIssuedAtFromToken(token: String): Date? {
        return try {
            getClaims(token)?.issuedAt
        } catch (e: Exception) {
            logger.debug("Failed to extract issued at time from token: ${e.message}")
            null
        }
    }

    /**
     * Extract expiration time from token
     */
    fun getExpirationFromToken(token: String): Date? {
        return try {
            getClaims(token)?.expiration
        } catch (e: Exception) {
            logger.debug("Failed to extract expiration time from token: ${e.message}")
            null
        }
    }

    /**
     * Get all claims from token as a map
     */
    fun getAllClaimsFromToken(token: String): Map<String, Any>? {
        return try {
            getClaims(token)
        } catch (e: Exception) {
            logger.debug("Failed to extract all claims from token: ${e.message}")
            null
        }
    }

    /**
     * Extract token type (access or refresh)
     */
    fun getTokenType(token: String): String? {
        return try {
            getClaims(token)?.get("type", String::class.java)
        } catch (e: Exception) {
            logger.debug("Failed to extract token type: ${e.message}")
            null
        }
    }

    /**
     * Check if token is an access token
     */
    fun isAccessToken(token: String): Boolean {
        return getTokenType(token) == TOKEN_TYPE_ACCESS
    }

    /**
     * Check if token is a refresh token
     */
    fun isRefreshToken(token: String): Boolean {
        return getTokenType(token) == TOKEN_TYPE_REFRESH
    }

    /**
     * Validate token against the given email
     */
    fun validateToken(token: String, email: String): Boolean {
        return try {
            val claims = getClaims(token)
            val tokenEmail = claims?.subject
            val isValid = tokenEmail == email && claims.expiration?.after(Date()) == true
            if (!isValid) {
                logger.debug("Token validation failed for email: $email")
            }
            isValid
        } catch (e: Exception) {
            logger.debug("Token validation error: ${e.message}")
            false
        }
    }

    /**
     * Validate access token specifically
     */
    fun validateAccessToken(token: String, email: String): Boolean {
        return validateToken(token, email) && isAccessToken(token)
    }

    /**
     * Validate refresh token specifically
     */
    fun validateRefreshToken(token: String, email: String): Boolean {
        return validateToken(token, email) && isRefreshToken(token)
    }

    /**
     * Check if token is expired
     */
    fun isTokenExpired(token: String): Boolean {
        return try {
            val expiration = getExpirationFromToken(token)
            expiration?.before(Date()) ?: true
        } catch (e: Exception) {
            logger.debug("Error checking token expiration: ${e.message}")
            true
        }
    }

    /**
     * Check if token needs refresh (approaching expiry)
     * Returns true if token will expire within the configured threshold
     */
    fun needsRefresh(token: String): Boolean {
        if (!jwtConfig.isAutoRefreshEnabled()) {
            return false
        }

        return try {
            val expiration = getExpirationFromToken(token) ?: return true
            val now = Date()
            val timeUntilExpiry = expiration.time - now.time

            // Token needs refresh if it expires within threshold
            timeUntilExpiry > 0 && timeUntilExpiry < jwtConfig.getAutoRefreshThresholdMs()
        } catch (e: Exception) {
            logger.debug("Error checking if token needs refresh: ${e.message}")
            false
        }
    }

    /**
     * Get time until token expiry in seconds
     */
    fun getTimeUntilExpirySeconds(token: String): Long {
        return try {
            val expiration = getExpirationFromToken(token) ?: return 0
            val now = Date()
            val timeUntilExpiry = expiration.time - now.time
            if (timeUntilExpiry > 0) timeUntilExpiry / 1000 else 0
        } catch (e: Exception) {
            logger.debug("Error getting time until expiry: ${e.message}")
            0
        }
    }

    /**
     * Get JwtConfig for external access
     */
    fun getConfig(): JwtConfig = jwtConfig

    private fun getClaims(token: String): Claims? {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            logger.debug("Failed to parse token: ${e.message}")
            null
        }
    }
}

/**
 * Data class representing a pair of Access and Refresh tokens
 */
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,  // in seconds
    val refreshTokenExpiresIn: Long, // in seconds
    val tokenType: String = "Bearer"
)

