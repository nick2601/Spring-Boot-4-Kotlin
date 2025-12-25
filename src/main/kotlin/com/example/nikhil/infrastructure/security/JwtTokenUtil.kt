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
     * Generate JWT token for the given email
     */
    fun generateToken(email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationTime)

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
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

