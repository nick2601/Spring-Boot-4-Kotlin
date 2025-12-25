package com.example.nikhil.infrastructure.web.controller

import com.example.nikhil.application.service.ProductService
import com.example.nikhil.infrastructure.persistence.entity.Category
import com.example.nikhil.infrastructure.web.dto.ProductDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Product Controller
 * REST endpoints for product and category management
 */
@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product and category management endpoints")
class ProductsController(
    private val productService: ProductService
) {

    // ==================== Product CRUD Endpoints ====================

    /**
     * Create a new product
     * POST /products
     */
    @PostMapping
    @Operation(summary = "Create a new product", description = "Creates a new product in the catalog")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Product created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid product data", content = [Content()])
        ]
    )
    fun createProduct(@Valid @RequestBody productDto: ProductDto): ResponseEntity<ProductDto> {
        val created = productService.createProduct(productDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    /**
     * Get all products, optionally filtered by categoryId
     * GET /products
     * GET /products?categoryId=1
     */
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves all products, optionally filtered by category")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Products retrieved successfully")
        ]
    )
    fun getAllProducts(
        @Parameter(description = "Filter by category ID", example = "1")
        @RequestParam(required = false) categoryId: Long?
    ): ResponseEntity<List<ProductDto>> {
        val products = productService.getAllProducts(categoryId)
        return ResponseEntity.ok(products)
    }

    /**
     * Get product by ID
     * GET /products/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a single product by its ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Product not found", content = [Content()])
        ]
    )
    fun getProductById(
        @Parameter(description = "Product ID", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<ProductDto> {
        val product = productService.getProductById(id)
        return ResponseEntity.ok(product)
    }

    /**
     * Update product by ID
     * PUT /products/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product by its ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Product updated successfully"),
            ApiResponse(responseCode = "404", description = "Product not found", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Invalid product data", content = [Content()])
        ]
    )
    fun updateProduct(
        @Parameter(description = "Product ID", required = true, example = "1")
        @PathVariable id: Long,
        @Valid @RequestBody productDto: ProductDto
    ): ResponseEntity<ProductDto> {
        val updated = productService.updateProduct(id, productDto)
        return ResponseEntity.ok(updated)
    }

    /**
     * Delete product by ID
     * DELETE /products/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Deletes a product by its ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            ApiResponse(responseCode = "404", description = "Product not found", content = [Content()])
        ]
    )
    fun deleteProduct(
        @Parameter(description = "Product ID", required = true, example = "1")
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }

    // ==================== Category Endpoints ====================

    /**
     * Get all categories
     * GET /products/categories
     */
    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieves all product categories")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
        ]
    )
    fun getAllCategories(): ResponseEntity<List<Category>> {
        val categories = productService.getAllCategories()
        return ResponseEntity.ok(categories)
    }

    /**
     * Get category by ID
     * GET /products/categories/{id}
     */
    @GetMapping("/categories/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves a single category by its ID")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Category not found", content = [Content()])
        ]
    )
    fun getCategoryById(
        @Parameter(description = "Category ID", required = true, example = "1")
        @PathVariable id: Byte
    ): ResponseEntity<Category> {
        val category = productService.getCategoryById(id)
        return ResponseEntity.ok(category)
    }
}