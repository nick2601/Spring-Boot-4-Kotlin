package com.example.nikhil.infrastructure.web.controller

import com.example.nikhil.application.service.AuthService
import com.example.nikhil.infrastructure.security.JwtTokenUtil
import com.example.nikhil.infrastructure.web.dto.AuthRequest
import com.example.nikhil.infrastructure.web.dto.AuthResponse
import com.example.nikhil.infrastructure.web.dto.RefreshTokenRequest
import com.example.nikhil.infrastructure.web.dto.UserDto
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

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User authentication endpoints")
class AuthController(
    private val authService: AuthService,
    private val jwtTokenUtil: JwtTokenUtil
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
            return ResponseEntity.status(401).body(
                mapOf(
                    "valid" to false,
                    "message" to "No Authorization header provided"
                )
            )
        }

        val token = authHeader.removePrefix("Bearer ").trim()

        if (token.isEmpty()) {
            return ResponseEntity.status(401).body(
                mapOf(
                    "valid" to false,
                    "message" to "No token provided"
                )
            )
        }

        return try {
            val email = authService.getEmailFromToken(token)
            if (email != null && authService.validateToken(token, email)) {
                // Extract all enhanced claims
                val userId = jwtTokenUtil.getUserIdFromToken(token)
                val name = jwtTokenUtil.getNameFromToken(token)
                val roles = jwtTokenUtil.getRolesFromToken(token)
                val tokenId = jwtTokenUtil.getTokenIdFromToken(token)
                val issuer = jwtTokenUtil.getIssuerFromToken(token)
                val issuedAt = jwtTokenUtil.getIssuedAtFromToken(token)
                val expiration = jwtTokenUtil.getExpirationFromToken(token)

                val response = mutableMapOf<String, Any>(
                    "valid" to true,
                    "email" to email,
                    "message" to "Token is valid"
                )

                userId?.let { response["userId"] = it }
                name?.let { response["name"] = it }
                if (roles.isNotEmpty()) response["roles"] = roles
                tokenId?.let { response["tokenId"] = it }
                issuer?.let { response["issuer"] = it }
                issuedAt?.let { response["issuedAt"] = it.toString() }
                expiration?.let { response["expiresAt"] = it.toString() }

                // Add token refresh information
                response["tokenType"] = jwtTokenUtil.getTokenType(token) ?: "unknown"
                response["timeUntilExpirySeconds"] = jwtTokenUtil.getTimeUntilExpirySeconds(token)
                response["needsRefresh"] = jwtTokenUtil.needsRefresh(token)
                response["autoRefreshEnabled"] = jwtTokenUtil.getConfig().isAutoRefreshEnabled()

                ResponseEntity.ok(response)
            } else {
                ResponseEntity.status(401).body(
                    mapOf(
                        "valid" to false,
                        "message" to "Token is invalid or expired"
                    )
                )
            }
        } catch (e: Exception) {
            ResponseEntity.status(401).body(
                mapOf(
                    "valid" to false,
                    "message" to "Token validation failed: ${e.message}"
                )
            )
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
            ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
            ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = [Content()])
        ]
    )
    fun refreshToken(
        @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<Any> {
        if (request.refreshToken.isBlank()) {
            return ResponseEntity.status(401).body(
                mapOf(
                    "success" to false,
                    "message" to "No refresh token provided"
                )
            )
        }

        return try {
            val response = authService.refreshTokens(request.refreshToken)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(401).body(
                mapOf(
                    "success" to false,
                    "message" to "Token refresh failed: ${e.message}"
                )
            )
        }
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
            return ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "No Authorization header provided"
                )
            )
        }

        val token = authHeader.removePrefix("Bearer ").trim()

        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "No token provided"
                )
            )
        }

        return try {
            // Use AuthService to get all token information
            val tokenInfo = authService.getTokenInfo(token)

            if (tokenInfo.email == null) {
                return ResponseEntity.badRequest().body(
                    mapOf(
                        "error" to "Failed to decode token"
                    )
                )
            }

            val response = mutableMapOf<String, Any>(
                "tokenStructure" to mapOf(
                    "header" to "JWT Header (algorithm and type)",
                    "payload" to "JWT Payload (claims)",
                    "signature" to "JWT Signature (encrypted)"
                ),
                "standardClaims" to mutableMapOf<String, Any?>().apply {
                    this["sub"] = tokenInfo.email
                    tokenInfo.tokenId?.let { this["jti"] = it }
                    tokenInfo.issuer?.let { this["iss"] = it }
                    tokenInfo.issuedAt?.let { this["iat"] = it.toString() }
                    tokenInfo.expiration?.let { this["exp"] = it.toString() }
                },
                "customClaims" to mutableMapOf<String, Any?>().apply {
                    tokenInfo.userId?.let { this["userId"] = it }
                    tokenInfo.name?.let { this["name"] = it }
                    if (tokenInfo.roles.isNotEmpty()) this["roles"] = tokenInfo.roles
                }
            )

            // Check if token is expired
            tokenInfo.expiration?.let {
                response["isExpired"] = it.before(java.util.Date())
                val now = java.util.Date()
                val timeLeft = it.time - now.time
                response["timeToExpiry"] = if (timeLeft > 0) {
                    "${timeLeft / 1000 / 60} minutes"
                } else {
                    "Expired ${-timeLeft / 1000 / 60} minutes ago"
                }
            }

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                mapOf(
                    "error" to "Failed to decode token: ${e.message}"
                )
            )
        }
    }
}

