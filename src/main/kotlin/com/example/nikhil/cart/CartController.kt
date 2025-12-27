package com.example.nikhil.cart

import com.example.nikhil.cart.dtos.AddToCartRequest
import com.example.nikhil.cart.dtos.CartDto
import com.example.nikhil.cart.dtos.UpdateCartItemRequest
import com.example.nikhil.cart.entity.CartStatus
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
 * Cart Controller
 * REST endpoints for shopping cart management
 * Primary operations are based on cartId
 */
@RestController
@RequestMapping("/carts")
@Tag(name = "Cart", description = "Shopping cart management endpoints")
class CartController(
    private val cartService: CartService
) {

    // ==================== Cart CRUD Endpoints ====================

    /**
     * Create a new cart for user
     * POST /carts?userId={userId}
     */
    @PostMapping
    @Operation(
        summary = "Create new cart",
        description = "Creates a new shopping cart for the specified user. Returns existing active cart if one exists."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Cart created successfully"),
            ApiResponse(responseCode = "404", description = "User not found", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Invalid request", content = [Content()])
        ]
    )
    fun createCart(
        @Parameter(description = "User ID", required = true, example = "1")
        @RequestParam userId: Long
    ): ResponseEntity<CartDto> {
        val cart = cartService.createCart(userId)
        return ResponseEntity.status(HttpStatus.CREATED).body(cart)
    }

    /**
     * Get cart by ID
     * GET /carts/{cartId}
     */
    @GetMapping("/{cartId}")
    @Operation(
        summary = "Get cart by ID",
        description = "Retrieves a cart with all its items by cart ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Cart not found", content = [Content()])
        ]
    )
    fun getCartById(
        @Parameter(description = "Cart ID", required = true, example = "1")
        @PathVariable cartId: Long
    ): ResponseEntity<CartDto> {
        val cart = cartService.getCartById(cartId)
        return ResponseEntity.ok(cart)
    }

    /**
     * Get cart by user ID (convenience endpoint)
     * GET /carts?userId={userId}
     */
    @GetMapping
    @Operation(
        summary = "Get cart by user ID",
        description = "Retrieves the active cart for a specific user"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            ApiResponse(responseCode = "404", description = "Cart not found for user", content = [Content()])
        ]
    )
    fun getCartByUserId(
        @Parameter(description = "User ID", required = true, example = "1")
        @RequestParam userId: Long
    ): ResponseEntity<CartDto> {
        val cart = cartService.getCartByUserId(userId)
        return ResponseEntity.ok(cart)
    }

    /**
     * Clear all items from cart
     * DELETE /carts/{cartId}/items
     */
    @DeleteMapping("/{cartId}/items")
    @Operation(
        summary = "Clear cart items",
        description = "Removes all items from the cart but keeps the cart active"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
            ApiResponse(responseCode = "404", description = "Cart not found", content = [Content()])
        ]
    )
    fun clearCart(
        @Parameter(description = "Cart ID", required = true, example = "1")
        @PathVariable cartId: Long
    ): ResponseEntity<CartDto> {
        val cart = cartService.clearCart(cartId)
        return ResponseEntity.ok(cart)
    }

    /**
     * Delete cart completely
     * DELETE /carts/{cartId}
     */
    @DeleteMapping("/{cartId}")
    @Operation(
        summary = "Delete cart",
        description = "Permanently deletes the cart and all its items"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Cart deleted successfully"),
            ApiResponse(responseCode = "404", description = "Cart not found", content = [Content()])
        ]
    )
    fun deleteCart(
        @Parameter(description = "Cart ID", required = true, example = "1")
        @PathVariable cartId: Long
    ): ResponseEntity<Void> {
        cartService.deleteCart(cartId)
        return ResponseEntity.noContent().build()
    }

    // ==================== Cart Item Endpoints ====================

    /**
     * Add item to cart
     * POST /carts/{cartId}/items
     */
    @PostMapping("/{cartId}/items")
    @Operation(
        summary = "Add item to cart",
        description = "Adds a product to the cart or updates quantity if product already exists"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Item added successfully"),
            ApiResponse(responseCode = "404", description = "Cart or product not found", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Invalid request", content = [Content()])
        ]
    )
    fun addItemToCart(
        @Parameter(description = "Cart ID", required = true, example = "1")
        @PathVariable cartId: Long,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Product details to add",
            required = true
        )
        @Valid @RequestBody request: AddToCartRequest
    ): ResponseEntity<CartDto> {
        val cart = cartService.addItemToCart(cartId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(cart)
    }

    /**
     * Update item quantity in cart
     * PUT /carts/{cartId}/items/{productId}
     */
    @PutMapping("/{cartId}/items/{productId}")
    @Operation(
        summary = "Update item quantity",
        description = "Updates the quantity of a specific product in the cart"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Quantity updated successfully"),
            ApiResponse(responseCode = "404", description = "Cart or product not found in cart", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Invalid quantity", content = [Content()])
        ]
    )
    fun updateItemQuantity(
        @Parameter(description = "Cart ID", required = true, example = "1")
        @PathVariable cartId: Long,
        @Parameter(description = "Product ID", required = true, example = "1")
        @PathVariable productId: Long,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "New quantity",
            required = true
        )
        @Valid @RequestBody request: UpdateCartItemRequest
    ): ResponseEntity<CartDto> {
        val cart = cartService.updateItemQuantity(cartId, productId, request)
        return ResponseEntity.ok(cart)
    }

    /**
     * Remove item from cart
     * DELETE /carts/{cartId}/items/{productId}
     */
    @DeleteMapping("/{cartId}/items/{productId}")
    @Operation(
        summary = "Remove item from cart",
        description = "Removes a specific product from the cart"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Item removed successfully"),
            ApiResponse(responseCode = "404", description = "Cart or product not found in cart", content = [Content()])
        ]
    )
    fun removeItemFromCart(
        @Parameter(description = "Cart ID", required = true, example = "1")
        @PathVariable cartId: Long,
        @Parameter(description = "Product ID", required = true, example = "1")
        @PathVariable productId: Long
    ): ResponseEntity<CartDto> {
        val cart = cartService.removeItemFromCart(cartId, productId)
        return ResponseEntity.ok(cart)
    }

    // ==================== Cart Status Endpoints ====================

    /**
     * Update cart status
     * PATCH /carts/{cartId}/status?status={status}
     */
    @PatchMapping("/{cartId}/status")
    @Operation(
        summary = "Update cart status",
        description = "Changes the cart status (ACTIVE, CHECKOUT, COMPLETED, ABANDONED)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Status updated successfully"),
            ApiResponse(responseCode = "404", description = "Cart not found", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Invalid status", content = [Content()])
        ]
    )
    fun updateCartStatus(
        @Parameter(description = "Cart ID", required = true, example = "1")
        @PathVariable cartId: Long,
        @Parameter(description = "New cart status", required = true, example = "CHECKOUT")
        @RequestParam status: CartStatus
    ): ResponseEntity<CartDto> {
        val cart = cartService.updateCartStatus(cartId, status)
        return ResponseEntity.ok(cart)
    }

    /**
     * Proceed to checkout
     * POST /carts/{cartId}/checkout
     */
    @PostMapping("/{cartId}/checkout")
    @Operation(
        summary = "Proceed to checkout",
        description = "Validates cart and moves it to checkout status. Publishes Kafka event."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Cart moved to checkout"),
            ApiResponse(responseCode = "400", description = "Cart is empty", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Cart not found", content = [Content()])
        ]
    )
    fun proceedToCheckout(
        @Parameter(description = "Cart ID", required = true, example = "1")
        @PathVariable cartId: Long
    ): ResponseEntity<CartDto> {
        val cart = cartService.checkoutCart(cartId)
        return ResponseEntity.ok(cart)
    }

    /**
     * Get cart summary for checkout
     * GET /carts/{cartId}/summary
     */
    @GetMapping("/{cartId}/summary")
    @Operation(
        summary = "Get cart summary",
        description = "Returns a summary of the cart including item count, total price, etc."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Cart summary retrieved"),
            ApiResponse(responseCode = "404", description = "Cart not found", content = [Content()])
        ]
    )
    fun getCartSummary(
        @Parameter(description = "Cart ID", required = true, example = "1")
        @PathVariable cartId: Long
    ): ResponseEntity<Map<String, Any?>> {
        val cart = cartService.getCartById(cartId)
        val summary = mapOf<String, Any?>(
            "cartId" to cartId,
            "itemCount" to cart.items.size,
            "totalItems" to cart.items.sumOf { it.quantity },
            "totalPrice" to cart.totalPrice,
            "status" to cart.status,
            "items" to cart.items.map {
                mapOf(
                    "productId" to it.productId,
                    "productName" to it.productName,
                    "quantity" to it.quantity,
                    "unitPrice" to it.productPrice,
                    "subtotal" to it.subtotal
                )
            }
        )
        return ResponseEntity.ok(summary)
    }
}