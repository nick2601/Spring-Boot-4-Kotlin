package com.example.nikhil.infrastructure.persistence.repository

import com.example.nikhil.infrastructure.persistence.entity.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for Profile entity CRUD operations
 */
@Repository
interface ProfileRepository : JpaRepository<Profile, Long>
