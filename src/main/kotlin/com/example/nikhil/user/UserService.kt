package com.example.nikhil.user

import com.example.nikhil.auth.dto.ChangePasswordRequest
import com.example.nikhil.auth.dto.RegisterUserRequest
import com.example.nikhil.user.mapper.UserMapper
import com.example.nikhil.user.entity.User
import com.example.nikhil.user.dto.UserDto
import com.example.nikhil.user.entity.RoleName
import com.example.nikhil.user.repository.RoleRepository
import com.example.nikhil.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * User Use Case
 * Handles all user-related business operations including lifecycle and registration
 */
@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

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
     * Register new user with password encoding and default CUSTOMER role
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
        // Assign default CUSTOMER role
        roleRepository.findByName(RoleName.ROLE_CUSTOMER).ifPresent { role ->
            user.addRole(role)
            logger.debug("Assigned ROLE_CUSTOMER to new user: ${request.email}")
        }
        val savedUser = userRepository.save(user)
        logger.info("User registered successfully with id: ${savedUser.id}")
        return userMapper.toDto(savedUser)
    }

    /**
     * Change user password
     */
    @Transactional
    fun changePassword(request: ChangePasswordRequest) {
        logger.info("Changing password for user id: ${request.id}")
        val user = userRepository.findById(request.id)
            .orElseThrow { NoSuchElementException("User not found with id: ${request.id}") }
        if (!passwordEncoder.matches(request.oldPassword, user.password)) {
            logger.warn("Password change failed: Invalid old password for user id: ${request.id}")
            throw IllegalArgumentException("Invalid old password")
        }
        user.password = passwordEncoder.encode(request.newPassword)
        userRepository.save(user)
        logger.info("Password changed successfully for user id: ${request.id}")
    }

    /**
     * Create new user (for admin use, no password logic)
     */
    @Transactional
    fun createUser(userDto: UserDto): UserDto {
        logger.info("Creating user: ${userDto.email}")
        val user = userMapper.toEntity(userDto)
        val savedUser = userRepository.save(user)
        return userMapper.toDto(savedUser)
    }

    /**
     * Update existing user (no password logic)
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
}
