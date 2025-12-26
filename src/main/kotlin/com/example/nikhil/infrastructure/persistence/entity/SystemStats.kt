package com.example.nikhil.infrastructure.persistence.entity

/**
 * Data class for system statistics
 */
data class SystemStats(
    val totalUsers: Long,
    val adminCount: Long,
    val customerCount: Long,
    val usersWithNoRoles: Long,
    val availableRoles: List<String>
)