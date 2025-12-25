package com.example.nikhil.infrastructure.web.controller

import com.example.nikhil.application.service.AuthService
import com.example.nikhil.application.service.UserService
import com.example.nikhil.infrastructure.web.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * User Controller
 * REST endpoints for user management and authentication
 */
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management and authentication endpoints")
class UserController(
    private val userService: UserService,
    private val authService: AuthService
) {

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and receive JWT token")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Login successful"),
            ApiResponse(responseCode = "401", description = "Invalid credentials", content = [Content()])
        ]
    )
    fun login(@Valid @RequestBody authRequest: AuthRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(authRequest)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get the currently authenticated user's details")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User details retrieved"),
            ApiResponse(responseCode = "401", description = "Not authenticated", content = [Content()])
        ]
    )
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserDto> {
        val email = authentication.name
        val user = userService.getUserByEmail(email)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(userService.getUserById(user.id!!))
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves all users with optional sorting")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Users retrieved successfully")
        ]
    )
    fun getAllUsers(
        @Parameter(description = "Field to sort by", example = "name")
        @RequestParam(required = false, defaultValue = "name") sortBy: String,
        @Parameter(description = "Sort order (asc/desc)", example = "desc")
        @RequestParam(required = false, defaultValue = "desc") order: String
    ): ResponseEntity<List<UserDto>> {
        val users = userService.getAllUsers(sortBy, order)
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a single user by their ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            ApiResponse(responseCode = "404", description = "User not found", content = [Content()])
        ]
    )
    fun getUserById(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<UserDto> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }

    @PostMapping
    @Operation(summary = "Register new user", description = "Creates a new user account")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "User registered successfully"),
            ApiResponse(responseCode = "400", description = "Invalid user data", content = [Content()]),
            ApiResponse(responseCode = "409", description = "Email already exists", content = [Content()])
        ]
    )
    fun registerUser(@Valid @RequestBody request: RegisterUserRequest): ResponseEntity<UserDto> {
        val created = userService.registerUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user by their ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User updated successfully"),
            ApiResponse(responseCode = "404", description = "User not found", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Invalid user data", content = [Content()])
        ]
    )
    fun updateUser(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable id: Long,
        @Valid @RequestBody userDto: UserDto
    ): ResponseEntity<UserDto> {
        val updated = userService.updateUser(id, userDto)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by their ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "User deleted successfully"),
            ApiResponse(responseCode = "404", description = "User not found", content = [Content()])
        ]
    )
    fun deleteUser(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/change-password")
    @Operation(summary = "Change password", description = "Changes a user's password")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Password changed successfully"),
            ApiResponse(responseCode = "401", description = "Invalid old password", content = [Content()]),
            ApiResponse(responseCode = "404", description = "User not found", content = [Content()])
        ]
    )
    fun changePassword(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable id: Long,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Void> {
        userService.changePassword(id, request.oldPassword, request.newPassword)
        return ResponseEntity.noContent().build()
    }
}

