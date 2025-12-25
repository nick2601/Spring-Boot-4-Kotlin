package com.example.nikhil.infrastructure.mapper

import com.example.nikhil.infrastructure.persistence.entity.Product
import com.example.nikhil.infrastructure.web.dto.ProductDto
import org.springframework.stereotype.Component

/**
 * Product Mapper
 * Single Responsibility: Handles conversion between Product entity and ProductDto
 */
@Component
class ProductMapper {

    /**
     * Convert Product entity to ProductDto
     */
    fun toDto(product: Product) = ProductDto(
        id = product.id,
        name = product.name,
        description = product.description,
        price = product.price,
        categoryId = product.category?.id
    )

    /**
     * Convert list of Product entities to list of ProductDto
     */
    fun toDtoList(products: Iterable<Product>): List<ProductDto> = products.map { toDto(it) }

    /**
     * Convert ProductDto to Product entity
     * Note: Category should be set separately via CategoryRepository
     */
    fun toEntity(productDto: ProductDto) = Product(
        id = productDto.id,
        name = productDto.name,
        description = productDto.description,
        price = productDto.price
    )
}

