package com.example.nikhil.infrastructure.web.controller

import com.example.nikhil.application.service.AdminService
import com.example.nikhil.application.service.UserService
import com.example.nikhil.infrastructure.persistence.entity.RoleName
import com.example.nikhil.infrastructure.web.dto.UserDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Admin Controller
 * REST endpoints for admin-only operations
 * All endpoints require ROLE_ADMIN
 *
 * This is the STANDARD approach for e-commerce backends:
 * - Same database, different API access based on roles
 * - ROLE_CUSTOMER: Can browse, buy, manage own orders
 * - ROLE_ADMIN: Full access including user management, product CRUD, order management
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Admin-only endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val userService: UserService,
    private val adminService: AdminService
) {

    @GetMapping("/users")
    @Operation(summary = "Get all users (Admin only)")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Users retrieved"),
            ApiResponse(responseCode = "403", description = "Access denied", content = [Content()])
        ]
    )
    fun getAllUsers(): ResponseEntity<List<UserDto>> {
        val users = userService.getAllUsers("name", "asc")
        return ResponseEntity.ok(users)
    }

    @PostMapping("/users/{userId}/roles/{roleName}")
    @Operation(summary = "Assign role to user (Admin only)")
    fun assignRole(
        @PathVariable userId: Long,
        @PathVariable roleName: String
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val role = adminService.parseRoleName(roleName)
            val user = adminService.assignRoleToUser(userId, role)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Role $roleName assigned to user ${user.email}",
                "userId" to userId,
                "roles" to user.getRoleNames()
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message.orEmpty()
            ))
        }
    }

    @DeleteMapping("/users/{userId}/roles/{roleName}")
    @Operation(summary = "Remove role from user (Admin only)")
    fun removeRole(
        @PathVariable userId: Long,
        @PathVariable roleName: String
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val role = adminService.parseRoleName(roleName)
            val user = adminService.removeRoleFromUser(userId, role)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Role $roleName removed from user ${user.email}",
                "userId" to userId,
                "roles" to user.getRoleNames()
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message.orEmpty()
            ))
        }
    }

    @GetMapping("/users/{userId}/roles")
    @Operation(summary = "Get user roles (Admin only)")
    fun getUserRoles(@PathVariable userId: Long): ResponseEntity<Map<String, Any>> {
        val user = adminService.getUserById(userId)

        return ResponseEntity.ok(mapOf(
            "userId" to userId,
            "email" to (user.email ?: ""),
            "roles" to user.getRoleNames(),
            "isAdmin" to user.isAdmin(),
            "isCustomer" to user.isCustomer()
        ))
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all available roles (Admin only)")
    fun getAllRoles(): ResponseEntity<List<String>> {
        return ResponseEntity.ok(adminService.getAllRoles().map { it.name })
    }

    @GetMapping("/stats")
    @Operation(summary = "Get system statistics (Admin only)")
    fun getStats(): ResponseEntity<Map<String, Any>> {
        val stats = adminService.getSystemStats()

        return ResponseEntity.ok(mapOf(
            "totalUsers" to stats.totalUsers,
            "adminCount" to stats.adminCount,
            "customerCount" to stats.customerCount,
            "usersWithNoRoles" to stats.usersWithNoRoles,
            "roles" to stats.availableRoles
        ))
    }
}

