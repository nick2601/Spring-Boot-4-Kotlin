package com.example.nikhil.user.repository

import com.example.nikhil.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for User entity CRUD operations
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun findByName(name: String): User?
    fun existsByEmail(email: String): Boolean
    fun deleteUserById(id: Long): Int
}
