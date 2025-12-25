package com.example.nikhil.infrastructure.persistence.repository

import com.example.nikhil.infrastructure.persistence.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Repository for Product entity CRUD operations
 */
@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findAllByCategoryId(categoryId: Long): List<Product>
    fun findByNameContainingIgnoreCase(name: String): List<Product>
}