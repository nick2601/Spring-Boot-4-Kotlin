package com.example.nikhil.cart.mapper

import com.example.nikhil.cart.entity.Cart
import com.example.nikhil.cart.dtos.CartDto
import com.example.nikhil.cart.dtos.CartItemDto
import com.example.nikhil.cart.entity.CartItem
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * Cart Mapper
 * Single Responsibility: Handles conversion between Cart entities and DTOs
 */
@Component
class CartMapper {

    /**
     * Convert Cart entity to CartDto
     */
    fun toDto(cart: Cart): CartDto {
        val items = cart.items.map { toItemDto(it) }
        val totalItems = items.sumOf { it.quantity }
        val totalPrice = items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.subtotal) }

        return CartDto(
            id = cart.id,
            userId = cart.user?.id,
            status = cart.status.name,
            items = items,
            totalItems = totalItems,
            totalPrice = totalPrice,
            createdAt = cart.createdAt,
            updatedAt = cart.updatedAt
        )
    }

    /**
     * Convert list of Cart entities to list of CartDto
     */
    fun toDtoList(carts: List<Cart>): List<CartDto> = carts.map { toDto(it) }

    /**
     * Convert CartItem entity to CartItemDto
     */
    fun toItemDto(item: CartItem): CartItemDto {
        val price = item.product?.price ?: BigDecimal.ZERO
        val subtotal = price.multiply(BigDecimal(item.quantity))

        return CartItemDto(
            id = item.id,
            productId = item.product?.id,
            productName = item.product?.name,
            productPrice = price,
            quantity = item.quantity,
            subtotal = subtotal,
            addedAt = item.addedAt
        )
    }

    /**
     * Convert list of CartItem entities to list of CartItemDto
     */
    fun toItemDtoList(items: List<CartItem>): List<CartItemDto> = items.map { toItemDto(it) }
}