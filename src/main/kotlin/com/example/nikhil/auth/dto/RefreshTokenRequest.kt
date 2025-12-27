package com.example.nikhil.auth.dto

import jakarta.validation.constraints.NotBlank

/**
 * DTO for refresh token request
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

