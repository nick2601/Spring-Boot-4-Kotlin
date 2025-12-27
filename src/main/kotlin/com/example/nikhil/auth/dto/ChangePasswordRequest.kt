package com.example.nikhil.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * DTO for password change requests
 */
data class ChangePasswordRequest(
    @field:NotBlank(message = "User ID is required")
    val id: Long,

    @field:NotBlank(message = "Old password is required")
    @field:Size(min = 6, max = 100, message = "Old password must be between 6 and 100 characters")
    val oldPassword: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters")
    val newPassword: String
)