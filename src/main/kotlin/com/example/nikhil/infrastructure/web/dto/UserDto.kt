package com.example.nikhil.infrastructure.web.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

/**
 * DTO for User data transfer
 * Used for responses and update operations
 */
data class UserDto(
    val id: Long? = null,

    @field:Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    val name: String? = null,

    @field:Email(message = "Invalid email format")
    val email: String? = null,

    @field:Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    val password: String? = null,

    val datetime: LocalDateTime? = null
)
