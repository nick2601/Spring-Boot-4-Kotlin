package com.example.nikhil.infrastructure.web.controller

import com.example.nikhil.application.service.AuthService
import com.example.nikhil.infrastructure.web.dto.AuthRequest
import com.example.nikhil.infrastructure.web.dto.AuthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication endpoints")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user and receive JWT token"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Login successful",
                content = [Content(schema = Schema(implementation = AuthResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
                content = [Content()]
            )
        ]
    )
    fun login(@Valid @RequestBody authRequest: AuthRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(authRequest)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/validate")
    @Operation(
        summary = "Validate JWT token",
        description = "Validates the JWT token from Authorization header and returns user info if valid"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Token is valid"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Token is invalid or expired",
                content = [Content()]
            )
        ]
    )
    fun validateToken(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): ResponseEntity<Map<String, Any>> {
        if (authHeader.isNullOrBlank()) {
            return ResponseEntity.status(401).body(mapOf(
                "valid" to false,
                "message" to "No Authorization header provided"
            ))
        }

        val token = authHeader.removePrefix("Bearer ").trim()

        if (token.isEmpty()) {
            return ResponseEntity.status(401).body(mapOf(
                "valid" to false,
                "message" to "No token provided"
            ))
        }

        return try {
            val email = authService.getEmailFromToken(token)
            if (email != null && authService.validateToken(token, email)) {
                ResponseEntity.ok(mapOf(
                    "valid" to true,
                    "email" to email,
                    "message" to "Token is valid"
                ))
            } else {
                ResponseEntity.status(401).body(mapOf(
                    "valid" to false,
                    "message" to "Token is invalid or expired"
                ))
            }
        } catch (e: Exception) {
            ResponseEntity.status(401).body(mapOf(
                "valid" to false,
                "message" to "Token validation failed: ${e.message}"
            ))
        }
    }

    @PostMapping("/logout")
    @Operation(
        summary = "User logout",
        description = "Logs out the user and invalidates the session. Note: For JWT, token invalidation should be handled client-side by removing the token."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Logout successful"
            )
        ]
    )
    fun logout(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): ResponseEntity<Map<String, Any>> {
        // For JWT-based auth, logout is typically handled client-side
        // by removing the token from storage.
        //
        // For enhanced security, you could:
        // 1. Maintain a blacklist of invalidated tokens
        // 2. Use short-lived tokens with refresh tokens
        // 3. Store tokens in Redis and remove on logout

        val email = authHeader?.let {
            val token = it.removePrefix("Bearer ").trim()
            if (token.isNotEmpty()) {
                authService.getEmailFromToken(token)
            } else null
        }

        // Publish logout event to Kafka
        email?.let {
            authService.publishLogoutEvent(it)
        }

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "Logout successful. Please remove the token from client storage.",
            "email" to (email ?: "unknown")
        ))
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh JWT token",
        description = "Generates a new JWT token if the current token is still valid"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Token refreshed"),
            ApiResponse(responseCode = "401", description = "Invalid or expired token", content = [Content()])
        ]
    )
    fun refreshToken(
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Any> {
        val token = authHeader.removePrefix("Bearer ").trim()

        if (token.isEmpty()) {
            return ResponseEntity.status(401).body(mapOf(
                "success" to false,
                "message" to "No token provided"
            ))
        }

        return try {
            val email = authService.getEmailFromToken(token)
            if (email != null && authService.validateToken(token, email)) {
                val newToken = authService.refreshToken(email)
                ResponseEntity.ok(AuthResponse(
                    token = newToken,
                    email = email,
                    name = null
                ))
            } else {
                ResponseEntity.status(401).body(mapOf(
                    "success" to false,
                    "message" to "Token is invalid or expired"
                ))
            }
        } catch (e: Exception) {
            ResponseEntity.status(401).body(mapOf(
                "success" to false,
                "message" to "Token refresh failed: ${e.message}"
            ))
        }
    }
}

