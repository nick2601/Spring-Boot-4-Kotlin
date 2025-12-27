package com.example.nikhil.auth

import com.example.nikhil.auth.dto.TokenPair
import com.example.nikhil.auth.util.JwtConfig
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT Service
 * Handles token generation, validation, and parsing
 * Supports both Access Tokens (short-lived) and Refresh Tokens (long-lived)
 * Uses JwtConfig for centralized configuration management
 */
@Service
class JwtService(
    private val jwtConfig: JwtConfig
) {
    private val logger = LoggerFactory.getLogger(JwtService::class.java)

    companion object {
        const val TOKEN_TYPE_ACCESS = "access"
        const val TOKEN_TYPE_REFRESH = "refresh"
    }

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun generateToken(email: String): String = generateAccessToken(email, null, null, emptyList())

    fun generateToken(email: String, userId: Long?, name: String?, roles: List<String>): String =
        generateAccessToken(email, userId, name, roles)

    fun generateAccessToken(email: String?, userId: Long?, name: String?, roles: List<String>): String =
        generateTokenInternal(email, userId, name, roles, TOKEN_TYPE_ACCESS, jwtConfig.accessToken.expiration)

    fun generateRefreshToken(email: String?, userId: Long?): String =
        generateTokenInternal(email, userId, null, emptyList(), TOKEN_TYPE_REFRESH, jwtConfig.refreshToken.expiration)

    fun generateTokenPair(email: String?, userId: Long?, name: String?, roles: List<String>): TokenPair {
        val accessToken = generateAccessToken(email, userId, name, roles)
        val refreshToken = generateRefreshToken(email, userId)
        return TokenPair(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtConfig.getAccessTokenExpirationSeconds(),
            refreshTokenExpiresIn = jwtConfig.getRefreshTokenExpirationSeconds()
        )
    }

    private fun generateTokenInternal(
        email: String?, userId: Long?, name: String?, roles: List<String>, tokenType: String, expirationTime: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationTime)
        val tokenId = UUID.randomUUID().toString()
        val builder =
            Jwts.builder().subject(email).issuedAt(now).expiration(expiryDate).id(tokenId).issuer(jwtConfig.issuer)
                .claim("type", tokenType).signWith(secretKey)
        userId?.let { builder.claim("userId", it) }
        name?.let { builder.claim("name", it) }
        if (roles.isNotEmpty()) builder.claim("roles", roles)
        return builder.compact()
    }

    fun getEmailFromToken(token: String): String? = try {
        getClaims(token)?.subject
    } catch (e: Exception) {
        logger.debug("Failed to extract email from token: ", e); null
    }

    fun getUserIdFromToken(token: String): Long? = try {
        getClaims(token)?.get("userId", Number::class.java)?.toLong()
    } catch (e: Exception) {
        logger.debug("Failed to extract userId from token: ", e); null
    }

    fun getNameFromToken(token: String): String? = try {
        getClaims(token)?.get("name", String::class.java)
    } catch (e: Exception) {
        logger.debug("Failed to extract name from token: ", e); null
    }

    @Suppress("UNCHECKED_CAST")
    fun getRolesFromToken(token: String): List<String> = try {
        getClaims(token)?.get("roles", List::class.java) as? List<String> ?: emptyList()
    } catch (e: Exception) {
        logger.debug("Failed to extract roles from token: ", e); emptyList()
    }

    fun getExpirationFromToken(token: String): Date? = try {
        getClaims(token)?.expiration
    } catch (e: Exception) {
        logger.debug("Failed to extract expiration time from token: ", e); null
    }

    fun getTokenType(token: String): String? = try {
        getClaims(token)?.get("type", String::class.java)
    } catch (e: Exception) {
        logger.debug("Failed to extract token type: ", e); null
    }

    fun isAccessToken(token: String): Boolean = getTokenType(token) == TOKEN_TYPE_ACCESS
    fun isRefreshToken(token: String): Boolean = getTokenType(token) == TOKEN_TYPE_REFRESH
    fun validateToken(token: String, email: String): Boolean = try {
        val claims = getClaims(token)
        val tokenEmail = claims?.subject
        val isValid = tokenEmail == email && claims.expiration?.after(Date()) == true
        if (!isValid) logger.debug("Token validation failed for email: $email")
        isValid
    } catch (e: Exception) {
        logger.debug("Token validation error: ", e); false
    }

    fun validateAccessToken(token: String, email: String): Boolean = validateToken(token, email) && isAccessToken(token)
    fun validateRefreshToken(token: String, email: String): Boolean =
        validateToken(token, email) && isRefreshToken(token)

    fun isTokenExpired(token: String): Boolean = try {
        val expiration = getExpirationFromToken(token); expiration?.before(Date()) ?: true
    } catch (e: Exception) {
        logger.debug("Error checking token expiration: ", e); true
    }

    fun needsRefresh(token: String): Boolean {
        if (!jwtConfig.isAutoRefreshEnabled()) return false
        return try {
            val expiration = getExpirationFromToken(token) ?: return true
            val now = Date()
            val timeUntilExpiry = expiration.time - now.time
            timeUntilExpiry > 0 && timeUntilExpiry < jwtConfig.getAutoRefreshThresholdMs()
        } catch (e: Exception) {
            logger.debug("Error checking if token needs refresh: ", e); false
        }
    }

    fun getTimeUntilExpirySeconds(token: String): Long = try {
        val expiration = getExpirationFromToken(token) ?: return 0
        val now = Date()
        val timeUntilExpiry = expiration.time - now.time
        if (timeUntilExpiry > 0) timeUntilExpiry / 1000 else 0
    } catch (e: Exception) {
        logger.debug("Error getting time until expiry: ", e); 0
    }

    private fun getClaims(token: String): Claims? = try {
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).payload
    } catch (e: Exception) {
        logger.debug("Failed to parse token: ", e); null
    }
}
