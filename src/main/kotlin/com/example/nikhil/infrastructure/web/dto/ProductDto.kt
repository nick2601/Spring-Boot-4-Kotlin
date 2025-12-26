package com.example.nikhil.infrastructure.web.dto

import jakarta.validation.constraints.*
import java.math.BigDecimal

/**
 * DTO for Product data transfer
 * Used for both request and response payloads
 */
data class ProductDto(
    val id: Long? = null,

    @field:NotBlank(message = "Product name is required")
    @field:Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    val name: String? = null,

    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,

    @field:Positive(message = "Price must be positive")
    @field:DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    @field:Digits(
        integer = 10,
        fraction = 2,
        message = "Price must have at most 10 integer digits and 2 decimal places"
    )
    val price: BigDecimal? = null,

    @field:Positive(message = "Category ID must be positive")
    val categoryId: Byte? = null
)