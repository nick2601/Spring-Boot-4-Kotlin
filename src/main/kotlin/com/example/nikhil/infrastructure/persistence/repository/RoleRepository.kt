package com.example.nikhil.infrastructure.persistence.repository

import com.example.nikhil.infrastructure.persistence.entity.Role
import com.example.nikhil.infrastructure.persistence.entity.RoleName
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoleRepository : JpaRepository<Role, Long> {

    /**
     * Find role by name
     */
    fun findByName(name: RoleName): Optional<Role>

    /**
     * Check if role exists by name
     */
    fun existsByName(name: RoleName): Boolean
}

