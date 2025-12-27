package com.example.nikhil.user.repository

import com.example.nikhil.user.entity.Role
import com.example.nikhil.user.entity.RoleName
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

