package com.example.nikhil.application.service

import com.example.nikhil.infrastructure.persistence.entity.RoleName
import com.example.nikhil.infrastructure.persistence.entity.SystemStats
import com.example.nikhil.infrastructure.persistence.entity.User
import com.example.nikhil.infrastructure.persistence.repository.RoleRepository
import com.example.nikhil.infrastructure.persistence.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Admin Service
 * Handles admin-only business operations
 *
 * Responsibilities:
 * - Role management (assign/remove roles)
 * - System statistics
 * - User administration
 */
@Service
@Transactional(readOnly = true)
class AdminService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) {
    private val logger = LoggerFactory.getLogger(AdminService::class.java)

    /**
     * Assign a role to a user
     */
    @Transactional
    fun assignRoleToUser(userId: Long, roleName: RoleName): User {
        logger.info("Assigning role $roleName to user $userId")

        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with id: $userId") }

        val role = roleRepository.findByName(roleName)
            .orElseThrow { NoSuchElementException("Role not found: $roleName") }

        if (user.hasRole(roleName)) {
            logger.warn("User $userId already has role $roleName")
            return user
        }

        user.addRole(role)
        val savedUser = userRepository.save(user)
        logger.info("Role $roleName assigned to user ${user.email}")

        return savedUser
    }

    /**
     * Remove a role from a user
     */
    @Transactional
    fun removeRoleFromUser(userId: Long, roleName: RoleName): User {
        logger.info("Removing role $roleName from user $userId")

        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with id: $userId") }

        val role = roleRepository.findByName(roleName)
            .orElseThrow { NoSuchElementException("Role not found: $roleName") }

        if (!user.hasRole(roleName)) {
            logger.warn("User $userId doesn't have role $roleName")
            return user
        }

        user.removeRole(role)
        val savedUser = userRepository.save(user)
        logger.info("Role $roleName removed from user ${user.email}")

        return savedUser
    }

    /**
     * Get user by ID with roles
     */
    fun getUserById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with id: $userId") }
    }

    /**
     * Get system statistics
     */
    fun getSystemStats(): SystemStats {
        val allUsers = userRepository.findAll()

        return SystemStats(
            totalUsers = allUsers.size.toLong(),
            adminCount = allUsers.count { it.isAdmin() }.toLong(),
            customerCount = allUsers.count { it.isCustomer() }.toLong(),
            usersWithNoRoles = allUsers.count { it.roles.isEmpty() }.toLong(),
            availableRoles = RoleName.entries.map { it.name }
        )
    }

    /**
     * Get all available roles
     */
    fun getAllRoles(): List<RoleName> = RoleName.entries

    /**
     * Parse role name from string
     */
    fun parseRoleName(roleName: String): RoleName {
        return try {
            RoleName.valueOf(roleName.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid role name: $roleName. Valid roles: ${RoleName.entries.map { it.name }}")
        }
    }
}



