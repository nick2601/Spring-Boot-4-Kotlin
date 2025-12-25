package com.example.nikhil.application.service

import com.example.nikhil.infrastructure.persistence.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Custom UserDetailsService implementation for Spring Security
 * Loads user-specific data for authentication
 */
@Service
class  CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    private val logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)

    override fun loadUserByUsername(email: String): UserDetails {
        logger.debug("Loading user by email: $email")

        val user = userRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found with email: $email").also {
                logger.warn("User not found with email: $email")
            }

        return User.builder()
            .username(user.email ?: throw UsernameNotFoundException("User email is null"))
            .password(user.password ?: throw IllegalStateException("User password is null"))
            .authorities("ROLE_USER")
            .build()
    }
}

