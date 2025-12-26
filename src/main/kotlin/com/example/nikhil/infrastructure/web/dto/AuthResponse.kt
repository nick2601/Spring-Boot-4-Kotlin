package com.example.nikhil.infrastructure.web.dto

/**
 * DTO for authentication response containing JWT token and user information
 */
data class AuthResponse(
    val token: String,
    val type: String = "Bearer",
    val email: String,
    val userId: Long? = null,
    val name: String? = null,
    val roles: List<String> = listOf("ROLE_USER"),
    val expiresIn: Long = 86400 // 24 hours in seconds
)

