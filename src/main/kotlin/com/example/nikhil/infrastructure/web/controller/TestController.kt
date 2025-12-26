package com.example.nikhil.infrastructure.web.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/test")
class TestController(
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/hash-password")
    fun hashPassword(@RequestParam password: String): ResponseEntity<Map<String, Any>> {
        val hash = passwordEncoder.encode(password) ?: "error"
        val result: Map<String, Any> = mapOf(
            "password" to password,
            "hash" to hash
        )
        return ResponseEntity.ok(result)
    }

    @PostMapping("/verify-password")
    fun verifyPassword(
        @RequestParam password: String,
        @RequestParam hash: String
    ): ResponseEntity<Map<String, Any>> {
        val matches: Boolean = passwordEncoder.matches(password, hash)
        val result: Map<String, Any> = mapOf(
            "password" to password,
            "hash" to hash,
            "matches" to matches
        )
        return ResponseEntity.ok(result)
    }
}

