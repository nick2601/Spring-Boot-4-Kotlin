package com.example.nikhil.product.repository

import com.example.nikhil.product.entity.Product
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