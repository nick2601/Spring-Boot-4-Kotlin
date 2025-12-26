package com.example.nikhil.infrastructure.web.dto

import jakarta.validation.constraints.NotBlank

/**
 * DTO for refresh token request
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

