package com.example.nikhil.cart

import com.example.nikhil.cart.dtos.AddToCartRequest
import com.example.nikhil.cart.dtos.CartDto
import com.example.nikhil.cart.dtos.UpdateCartItemRequest
import com.example.nikhil.cart.entity.CartStatus
import com.example.nikhil.user.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * Cart Controller
 * REST endpoints for shopping cart management
 * Primary operations are based on cartId
 */
@RestController
@RequestMapping("/carts")
@Tag(name = "Cart", description = "Shopping cart management endpoints")
class CartController(
    private val cartService: CartService,
    private val userService: UserService
) {

    private val logger = LoggerFactory.getLogger(CartController::class.java)

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
     */
    @DeleteMapping("/{cartId}/items")
    fun clearCart(
        @PathVariable cartId: Long,
        authentication: Authentication
    ): ResponseEntity<CartDto> {
        assertCartOwnership(cartId, authentication)
        val cart = cartService.clearCart(cartId)
        return ResponseEntity.ok(cart)
    }

    /**
     * Delete cart completely
     */
    @DeleteMapping("/{cartId}")
    fun deleteCart(
        @PathVariable cartId: Long,
        authentication: Authentication
    ): ResponseEntity<Void> {
        assertCartOwnership(cartId, authentication)
        cartService.deleteCart(cartId)
        return ResponseEntity.noContent().build()
    }

    // ==================== Cart Item Endpoints ====================

    /**
     * Add item to cart
     * POST /carts/{cartId}/items
     * Must be performed by the cart owner (authenticated user)
     */
    @PostMapping("/{cartId}/items")
    fun addItemToCart(
        @PathVariable cartId: Long,
        @Valid @RequestBody request: AddToCartRequest,
        authentication: Authentication
    ): ResponseEntity<CartDto> {
        assertCartOwnership(cartId, authentication)
        val cart = cartService.addItemToCart(cartId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(cart)
    }

    /**
     * Update item quantity in cart
     */
    @PutMapping("/{cartId}/items/{productId}")
    fun updateItemQuantity(
        @PathVariable cartId: Long,
        @PathVariable productId: Long,
        @Valid @RequestBody request: UpdateCartItemRequest,
        authentication: Authentication
    ): ResponseEntity<CartDto> {
        assertCartOwnership(cartId, authentication)
        val cart = cartService.updateItemQuantity(cartId, productId, request)
        return ResponseEntity.ok(cart)
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/{cartId}/items/{productId}")
    fun removeItemFromCart(
        @PathVariable cartId: Long,
        @PathVariable productId: Long,
        authentication: Authentication
    ): ResponseEntity<CartDto> {
        assertCartOwnership(cartId, authentication)
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
     * POST /carts/{cartId}/checkout?maxTotal={maxTotal}
     * UserId is derived from the authenticated principal
     */
    @PostMapping("/{cartId}/checkout")
    fun proceedToCheckout(
        @PathVariable cartId: Long,
        @RequestParam(required = false) maxTotal: BigDecimal?,
        authentication: Authentication
    ): ResponseEntity<CartDto> {
        val userDto = userService.getUserDtoByEmail(authentication.name)
        val userId = userDto.id ?: throw IllegalStateException("Authenticated user has no id")

        assertCartOwnership(cartId, authentication)

        val cart = cartService.checkoutCart(cartId, userId, maxTotal)
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

    private fun assertCartOwnership(cartId: Long, authentication: Authentication) {
        val cart = cartService.getCartById(cartId)
        val authEmail = authentication.name
        val ownerEmail = userService.getUserById(cart.userId ?: -1L).email
        if (ownerEmail == null || ownerEmail.lowercase() != authEmail.lowercase()) {
            logger.warn("Unauthorized access attempt by $authEmail on cart $cartId owned by $ownerEmail")
            throw org.springframework.security.access.AccessDeniedException("You are not authorized to access this cart")
        }
    }
}