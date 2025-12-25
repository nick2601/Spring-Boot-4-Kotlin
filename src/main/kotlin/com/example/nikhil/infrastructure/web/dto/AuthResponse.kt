package com.example.nikhil.infrastructure.web.dto

/**
 * DTO for authentication response containing JWT token
 */
data class AuthResponse(
    val token: String,
    val type: String = "Bearer",
    val email: String,
    val name: String? = null,
    val expiresIn: Long = 86400 // 24 hours in seconds
)

