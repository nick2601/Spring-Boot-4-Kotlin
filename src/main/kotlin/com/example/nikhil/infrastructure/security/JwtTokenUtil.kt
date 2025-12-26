package com.example.nikhil.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT Token Utility
 * Handles token generation, validation, and parsing
 */
@Component
class JwtTokenUtil(
    @Value("\${jwt.secret:MySecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLongForHS256Algorithm}")
    private val secret: String,

    @Value("\${jwt.expiration:86400000}")
    private val expirationTime: Long
) {
    private val logger = LoggerFactory.getLogger(JwtTokenUtil::class.java)

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * Generate JWT token for the given email (backward compatible)
     */
    fun generateToken(email: String): String {
        return generateToken(email, null, null, emptyList())
    }

    /**
     * Generate JWT token with enhanced claims
     * @param email User's email (subject)
     * @param userId User's ID
     * @param name User's name
     * @param roles User's roles/authorities
     */
    fun generateToken(email: String, userId: Long?, name: String?, roles: List<String>): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationTime)
        val tokenId = UUID.randomUUID().toString()

        val builder = Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(expiryDate)
            .id(tokenId) // jti - JWT ID for token tracking
            .issuer("learning-spring-rest-api") // iss - token issuer
            .signWith(secretKey)

        // Add custom claims if available
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

