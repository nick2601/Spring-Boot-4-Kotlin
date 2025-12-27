package com.example.nikhil.user.entity

/**
 * Enum for role names
 * Using ROLE_ prefix as required by Spring Security
 */
enum class RoleName {
    ROLE_CUSTOMER,  // Default role for regular users
    ROLE_ADMIN      // Admin role with elevated privileges
}
