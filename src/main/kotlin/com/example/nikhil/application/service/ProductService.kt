package com.example.nikhil.application.service

import com.example.nikhil.infrastructure.mapper.ProductMapper
import com.example.nikhil.infrastructure.persistence.entity.Category
import com.example.nikhil.infrastructure.persistence.repository.CategoryRepository
import com.example.nikhil.infrastructure.persistence.repository.ProductRepository
import com.example.nikhil.infrastructure.web.dto.ProductDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Product Use Case
 * Handles all product-related business operations
 */
@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val productMapper: ProductMapper
) {
    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    // ==================== Product CRUD Operations ====================

    /**
     * Create new product with category assignment
     * @throws NoSuchElementException if categoryId is provided but category not found
     */
    @Transactional
    fun createProduct(productDto: ProductDto): ProductDto {
        logger.info("Creating product: ${productDto.name}")

        val product = productMapper.toEntity(productDto)

        // Assign category if categoryId is provided
        productDto.categoryId?.let { categoryId ->
            val category = categoryRepository.findById(categoryId)
                .orElseThrow { NoSuchElementException("Category not found with id: $categoryId") }
            product.category = category
            logger.debug("Assigned category: ${category.name} to product")
        }

        val savedProduct = productRepository.save(product)
        logger.info("Product created with id: ${savedProduct.id}")
        return productMapper.toDto(savedProduct)
    }

    /**
     * Get all products, optionally filtered by category
     */
    fun getAllProducts(categoryId: Long? = null): List<ProductDto> {
        logger.debug("Fetching products with categoryId: $categoryId")
        val products = categoryId?.let {
            productRepository.findAllByCategoryId(it)
        } ?: productRepository.findAll()
        return productMapper.toDtoList(products)
    }

    /**
     * Get product by ID
     * @throws NoSuchElementException if product not found
     */
    fun getProductById(id: Long): ProductDto {
        logger.debug("Fetching product with id: $id")
        val product = productRepository.findById(id)
            .orElseThrow { NoSuchElementException("Product not found with id: $id") }
        return productMapper.toDto(product)
    }

    /**
     * Update existing product with optional category update
     * @throws NoSuchElementException if product or category not found
     */
    @Transactional
    fun updateProduct(id: Long, productDto: ProductDto): ProductDto {
        logger.info("Updating product with id: $id")
        val existingProduct = productRepository.findById(id)
            .orElseThrow { NoSuchElementException("Product not found with id: $id") }

        existingProduct.apply {
            productDto.name?.let { name = it }
            productDto.description?.let { description = it }
            productDto.price?.let { price = it }
        }

        // Update category if categoryId is provided
        productDto.categoryId?.let { categoryId ->
            val category = categoryRepository.findById(categoryId)
                .orElseThrow { NoSuchElementException("Category not found with id: $categoryId") }
            existingProduct.category = category
            logger.debug("Updated category to: ${category.name}")
        }

        val updatedProduct = productRepository.save(existingProduct)
        logger.info("Product updated: $id")
        return productMapper.toDto(updatedProduct)
    }

    /**
     * Delete product by ID
     * @throws NoSuchElementException if product not found
     */
    @Transactional
    fun deleteProduct(id: Long) {
        logger.info("Deleting product with id: $id")
        if (!productRepository.existsById(id)) {
            throw NoSuchElementException("Product not found with id: $id")
        }
        productRepository.deleteById(id)
        logger.info("Product deleted: $id")
    }

    // ==================== Category Operations ====================

    /**
     * Get all categories
     */
    fun getAllCategories(): List<Category> {
        logger.debug("Fetching all categories")
        return categoryRepository.findAll()
    }

    /**
     * Get category by ID
     * @throws NoSuchElementException if category not found
     */
    fun getCategoryById(id: Byte): Category {
        logger.debug("Fetching category with id: $id")
        return categoryRepository.findById(id)
            .orElseThrow { NoSuchElementException("Category not found with id: $id") }
    }
}

