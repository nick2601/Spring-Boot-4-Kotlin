package com.example.nikhil.auth.dto

import java.util.*

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

/**
 * Token Information DTO
 * Contains all JWT claims and metadata
 */
data class TokenInfo(
    val email: String?,
    val userId: Long?,
    val name: String?,
    val roles: List<String>,
    val tokenId: String?,
    val issuer: String?,
    val issuedAt: Date?,
    val expiration: Date?
)

/**
 * Token Validation Response DTO
 * Contains validation status and token details
 */
data class TokenValidationResponse(
    val valid: Boolean,
    val message: String,
    val email: String? = null,
    val userId: Long? = null,
    val name: String? = null,
    val roles: List<String>? = null,
    val expiresAt: String? = null,
    val tokenType: String? = null,
    val timeUntilExpirySeconds: Long? = null,
    val needsRefresh: Boolean? = null
)
