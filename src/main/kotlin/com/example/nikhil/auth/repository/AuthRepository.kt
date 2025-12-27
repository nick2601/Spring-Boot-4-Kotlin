package com.example.nikhil.auth.repository

import com.example.nikhil.user.entity.User

interface AuthRepository {
    fun findByEmail(email: String): User?
}

