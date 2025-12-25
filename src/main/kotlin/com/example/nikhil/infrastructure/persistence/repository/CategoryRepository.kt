package com.example.nikhil.infrastructure.persistence.repository

import com.example.nikhil.infrastructure.persistence.entity.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for Category entity CRUD operations
 */
@Repository
interface CategoryRepository : JpaRepository<Category, Byte> {
    fun findByName(name: String): Category?
}
