package com.example.nikhil.auth.repository

import com.example.nikhil.user.entity.User
import com.example.nikhil.user.repository.UserRepository
import org.springframework.stereotype.Repository

@Repository
class AuthRepositoryImpl(
    private val userRepository: UserRepository
) : AuthRepository {
    override fun findByEmail(email: String): User? = userRepository.findByEmail(email)
}

