package com.example.nikhil.product.mapper

import com.example.nikhil.product.dtos.ProductDto
import com.example.nikhil.product.entity.Product
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