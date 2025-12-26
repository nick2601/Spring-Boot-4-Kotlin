package com.example.nikhil.infrastructure.web.dto

import java.time.LocalDateTime

/**
 * Standard error response structure for API errors
 */
data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String
)

/**
 * Validation error response with field-level errors
 */
data class ValidationErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val errors: Map<String, String>
)

