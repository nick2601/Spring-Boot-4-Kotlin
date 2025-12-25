package com.example.nikhil.infrastructure.persistence.repository

import com.example.nikhil.infrastructure.persistence.entity.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for Address entity CRUD operations
 */
@Repository
interface AddressRepository : JpaRepository<Address, Long> {
    fun findByUserId(userId: Long): List<Address>
}
