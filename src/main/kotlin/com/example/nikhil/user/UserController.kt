package com.example.nikhil.user

import com.example.nikhil.user.dto.UserDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * User Controller
 * REST endpoints for user management and authentication
 */
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management and authentication endpoints")
class UserController(
    private val userService: UserService
) {
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
}