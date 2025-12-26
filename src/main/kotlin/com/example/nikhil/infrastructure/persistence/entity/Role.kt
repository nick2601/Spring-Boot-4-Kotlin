package com.example.nikhil.infrastructure.persistence.entity

import jakarta.persistence.*

/**
 * Role entity - represents user roles for authorization
 * Available roles: CUSTOMER, ADMIN
 */
@Entity
@Table(name = "roles")
class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,

    @Column(name = "name", nullable = false, unique = true, length = 50)
    @Enumerated(EnumType.STRING)
    var name: RoleName = RoleName.ROLE_CUSTOMER
) {
    override fun toString(): String = "Role(id=$id, name=$name)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Role) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}

/**
 * Enum for role names
 * Using ROLE_ prefix as required by Spring Security
 */
enum class RoleName {
    ROLE_CUSTOMER,  // Default role for regular users
    ROLE_ADMIN      // Admin role with elevated privileges
}

