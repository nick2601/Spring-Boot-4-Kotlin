package com.example.nikhil.application.service

import com.example.nikhil.infrastructure.mapper.UserMapper
import com.example.nikhil.infrastructure.persistence.entity.User
import com.example.nikhil.infrastructure.persistence.repository.UserRepository
import com.example.nikhil.infrastructure.web.dto.RegisterUserRequest
import com.example.nikhil.infrastructure.web.dto.UserDto
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * User Use Case
 * Handles all user-related business operations
 */
@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    /**
     * Register new user with password encoding
     */
    @Transactional
    fun registerUser(request: RegisterUserRequest): UserDto {
        logger.info("Registering new user with email: ${request.email}")

        if (userRepository.existsByEmail(request.email)) {
            logger.warn("Registration failed: Email already exists: ${request.email}")
            throw IllegalArgumentException("Email already exists: ${request.email}")
        }

        val user = User(
            name = request.name,
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )

        val savedUser = userRepository.save(user)
        logger.info("User registered successfully with id: ${savedUser.id}")
        return userMapper.toDto(savedUser)
    }

    /**
     * Get all users with optional sorting
     */
    fun getAllUsers(sortBy: String = "id", order: String = "asc"): List<UserDto> {
        logger.debug("Fetching all users, sortBy: $sortBy, order: $order")
        val users = userRepository.findAll()

        val sorted = when (sortBy.lowercase()) {
            "name" -> if (order.lowercase() == "desc") users.sortedByDescending { it.name } else users.sortedBy { it.name }
            "email" -> if (order.lowercase() == "desc") users.sortedByDescending { it.email } else users.sortedBy { it.email }
            else -> if (order.lowercase() == "desc") users.sortedByDescending { it.id } else users.sortedBy { it.id }
        }

        return userMapper.toDtoList(sorted)
    }

    /**
     * Get user by ID
     * @throws NoSuchElementException if user not found
     */
    fun getUserById(id: Long): UserDto {
        logger.debug("Fetching user with id: $id")
        val user = userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User not found with id: $id") }
        return userMapper.toDto(user)
    }

    /**
     * Get user entity by email
     */
    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    /**
     * Get user DTO by email
     * @throws NoSuchElementException if user not found
     */
    fun getUserDtoByEmail(email: String): UserDto {
        logger.debug("Fetching user with email: $email")
        val user = userRepository.findByEmail(email)
            ?: throw NoSuchElementException("User not found with email: $email")
        return userMapper.toDto(user)
    }

    /**
     * Get user entity by name
     */
    fun getUserByName(name: String): User? {
        return userRepository.findByName(name)
    }

    /**
     * Create new user (without password - for admin use)
     */
    @Transactional
    fun createUser(userDto: UserDto): UserDto {
        logger.info("Creating user: ${userDto.email}")
        val user = userMapper.toEntity(userDto)
        val savedUser = userRepository.save(user)
        return userMapper.toDto(savedUser)
    }

    /**
     * Update existing user
     * @throws NoSuchElementException if user not found
     */
    @Transactional
    fun updateUser(id: Long, userDto: UserDto): UserDto {
        logger.info("Updating user with id: $id")
        val existingUser = userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User not found with id: $id") }

        existingUser.apply {
            userDto.name?.let { name = it }
            userDto.email?.let { email = it }
            userDto.password?.let { password = passwordEncoder.encode(it) }
        }

        val updatedUser = userRepository.save(existingUser)
        logger.info("User updated: $id")
        return userMapper.toDto(updatedUser)
    }

    /**
     * Delete user by ID
     * @throws NoSuchElementException if user not found
     */
    @Transactional
    fun deleteUser(id: Long) {
        logger.info("Deleting user with id: $id")
        if (!userRepository.existsById(id)) {
            throw NoSuchElementException("User not found with id: $id")
        }
        userRepository.deleteById(id)
        logger.info("User deleted: $id")
    }

    /**
     * Change user password
     * @throws NoSuchElementException if user not found
     * @throws IllegalArgumentException if old password doesn't match
     */
    @Transactional
    fun changePassword(id: Long, oldPassword: String, newPassword: String) {
        logger.info("Changing password for user id: $id")

        val user = userRepository.findById(id)
            .orElseThrow { NoSuchElementException("User not found with id: $id") }

        // Verify old password matches
        if (!passwordEncoder.matches(oldPassword, user.password)) {
            logger.warn("Password change failed: Invalid old password for user id: $id")
            throw IllegalArgumentException("Invalid old password")
        }

        // Encode and save new password
        user.password = passwordEncoder.encode(newPassword)
        userRepository.save(user)
        logger.info("Password changed successfully for user id: $id")
    }
}

