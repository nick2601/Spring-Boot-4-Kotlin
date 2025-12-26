package com.example.nikhil.infrastructure.web.dto

/**
 * DTO for authentication response containing JWT access and refresh tokens
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer",
    val email: String,
    val userId: Long? = null,
    val name: String? = null,
    val roles: List<String> = listOf("ROLE_USER"),
    val accessTokenExpiresIn: Long = 900,     // 15 minutes in seconds
    val refreshTokenExpiresIn: Long = 604800  // 7 days in seconds
)

