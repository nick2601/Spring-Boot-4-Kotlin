package com.example.nikhil.user.repository

import com.example.nikhil.user.entity.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for Profile entity CRUD operations
 */
@Repository
interface ProfileRepository : JpaRepository<Profile, Long>
