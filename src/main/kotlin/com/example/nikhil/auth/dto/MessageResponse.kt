package com.example.nikhil.auth.dto

/**
 * Simple message wrapper for API responses
 * Used for simple text responses from endpoints
 */
data class MessageResponse(
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

