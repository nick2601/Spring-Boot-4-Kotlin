package com.example.nikhil.auth

import com.example.nikhil.auth.dto.AuthRequest
import com.example.nikhil.auth.dto.AuthResponse
import com.example.nikhil.auth.dto.ChangePasswordRequest
import com.example.nikhil.auth.dto.RefreshTokenRequest
import com.example.nikhil.auth.dto.RegisterUserRequest
import com.example.nikhil.auth.dto.TokenValidationResponse
import com.example.nikhil.user.dto.UserDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.Date

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

    @GetMapping("/me")
    @Operation(
        summary = "Get current user",
        description = "Get the currently authenticated user's details from JWT token"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User details retrieved successfully",
                content = [Content(schema = Schema(implementation = UserDto::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Not authenticated or invalid token",
                content = [Content()]
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content()]
            )
        ]
    )
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserDto> {
        val email = authentication.name
        val user = authService.getCurrentUser(email)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/validate")
    @Operation(
        summary = "Validate JWT token",
        description = "Validates the JWT token from Authorization header and returns all token claims if valid"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Token is valid",
                content = [Content(schema = Schema(implementation = TokenValidationResponse::class))]
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
    ): ResponseEntity<TokenValidationResponse> {
        if (authHeader.isNullOrBlank()) {
            return ResponseEntity.status(401).body(
                TokenValidationResponse(valid = false, message = "No Authorization header provided")
            )
        }

        val token = authHeader.removePrefix("Bearer ").trim()

        if (token.isEmpty()) {
            return ResponseEntity.status(401).body(
                TokenValidationResponse(valid = false, message = "No token provided")
            )
        }

        val response = authService.validateTokenDetailed(token)
        return if (response.valid) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(401).body(response)
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
        val email = authHeader?.let {
            val token = it.removePrefix("Bearer ").trim()
            if (token.isNotEmpty()) {
                authService.getEmailFromToken(token)
            } else null
        }

        email?.let {
            authService.publishLogoutEvent(it)
        }

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Logout successful. Please remove the token from client storage.",
                "email" to (email ?: "unknown")
            )
        )
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh JWT tokens",
        description = "Use a valid refresh token to get new access and refresh tokens"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Tokens refreshed successfully", content = [Content(schema = Schema(implementation = AuthResponse::class))]),
            ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = [Content()])
        ]
    )
    fun refreshToken(
        @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<AuthResponse> {
        val response = authService.refreshTokens(request.refreshToken)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/token-info")
    @Operation(
        summary = "Get detailed token information",
        description = "Decode and display all claims and metadata from the JWT token"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Token decoded successfully"),
            ApiResponse(responseCode = "400", description = "Invalid token", content = [Content()])
        ]
    )
    fun getTokenInfo(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): ResponseEntity<Map<String, Any>> {
        if (authHeader.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "No Authorization header provided"))
        }

        val token = authHeader.removePrefix("Bearer ").trim()

        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "No token provided"))
        }

        val tokenInfo = authService.getTokenInfo(token)

        if (tokenInfo.email == null) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Failed to decode token"))
        }

        val response = mutableMapOf<String, Any>(
            "standardClaims" to mutableMapOf<String, Any?>().apply {
                this["sub"] = tokenInfo.email
                tokenInfo.expiration?.let { this["exp"] = it.toString() }
            },
            "customClaims" to mutableMapOf<String, Any?>().apply {
                tokenInfo.userId?.let { this["userId"] = it }
                tokenInfo.name?.let { this["name"] = it }
                if (tokenInfo.roles.isNotEmpty()) this["roles"] = tokenInfo.roles
            }
        )

        tokenInfo.expiration?.let { exp ->
            val now = Date()
            response["isExpired"] = exp.before(now)
            val timeLeft = exp.time - now.time
            response["timeToExpiry"] = if (timeLeft > 0) {
                "${timeLeft / 1000 / 60} minutes"
            } else {
                "Expired ${-timeLeft / 1000 / 60} minutes ago"
            }
        }

        return ResponseEntity.ok(response)
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Registers a new user with the provided details")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User registered successfully", content = [Content(schema = Schema(implementation = UserDto::class))]),
            ApiResponse(responseCode = "400", description = "Invalid input or email already exists", content = [Content()])
        ]
    )
    fun registerUser(@RequestBody request: RegisterUserRequest): ResponseEntity<UserDto> {
        val user = authService.registerUser(request)
        return ResponseEntity.ok(user)
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes the password for the authenticated user")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Password changed successfully"),
            ApiResponse(responseCode = "400", description = "Invalid old password or input", content = [Content()])
        ]
    )
    fun changePassword(@Valid @RequestBody request: ChangePasswordRequest): ResponseEntity<String> {
        authService.changePassword(request)
        return ResponseEntity.ok("Password changed successfully")
    }
}
