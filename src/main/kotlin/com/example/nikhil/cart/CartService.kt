package com.example.nikhil.cart

import com.example.nikhil.cart.dtos.AddToCartRequest
import com.example.nikhil.cart.dtos.CartDto
import com.example.nikhil.cart.dtos.UpdateCartItemRequest
import com.example.nikhil.common.kafka.event.OrderAction
import com.example.nikhil.common.kafka.event.OrderEvent
import com.example.nikhil.common.kafka.producer.KafkaProducerService
import com.example.nikhil.cart.mapper.CartMapper
import com.example.nikhil.cart.entity.Cart
import com.example.nikhil.cart.entity.CartItem
import com.example.nikhil.cart.entity.CartStatus
import com.example.nikhil.cart.repository.CartItemRepository
import com.example.nikhil.cart.repository.CartRepository
import com.example.nikhil.product.repository.ProductRepository
import com.example.nikhil.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

// ...existing code from CartService.kt, with updated package and imports...

/**
 * Cart Service
 * Single Responsibility: Handles cart-related business operations
 * Primary operations are based on cartId
 * Publishes Kafka events for all cart actions
 */
@Service
@Transactional(readOnly = true)
class CartService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val cartMapper: CartMapper,
    private val kafkaProducerService: KafkaProducerService
) {
    private val logger = LoggerFactory.getLogger(CartService::class.java)

    /**
     * Create a new cart for user
     */
    @Transactional
    fun createCart(userId: Long): CartDto {
        logger.info("Creating cart for user: $userId")

        // Check if user already has an active cart
        val existingCart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
        if (existingCart.isPresent) {
            logger.debug("User $userId already has an active cart")
            return cartMapper.toDto(existingCart.get())
        }

        val user =
            userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found with id: $userId") }

        val cart = cartRepository.save(Cart(user = user, status = CartStatus.ACTIVE))
        logger.info("Cart created with id: ${cart.id} for user: $userId")

        // Publish cart created event
        publishCartEvent(cart.id!!, userId, "CART_CREATED", "Cart created for user")

        return cartMapper.toDto(cart)
    }

    /**
     * Get cart by ID
     */
    fun getCartById(cartId: Long): CartDto {
        logger.debug("Fetching cart with id: $cartId")
        val cart = getCartWithItemsOrThrow(cartId)
        return cartMapper.toDto(cart)
    }

    /**
     * Get user's active cart
     */
    fun getCartByUserId(userId: Long): CartDto {
        logger.debug("Fetching cart for user: $userId")
        val cart = cartRepository.findByUserIdWithItems(userId, CartStatus.ACTIVE)
            .orElseThrow { NoSuchElementException("Cart not found for user: $userId") }
        return cartMapper.toDto(cart)
    }

    /**
     * Clear all items from cart by cartId
     */
    @Transactional
    fun clearCart(cartId: Long): CartDto {
        logger.info("Clearing cart: $cartId")
        val cart = getCartOrThrow(cartId)

        val userId = cart.user?.id ?: 0L
        cartItemRepository.deleteAllByCartId(cartId)
        cart.items.clear()

        val updatedCart = cartRepository.save(cart)
        logger.info("Cart cleared: $cartId")

        // Publish cart cleared event
        publishCartEvent(cartId, userId, "CART_CLEARED", "All items removed from cart")

        return cartMapper.toDto(updatedCart)
    }

    /**
     * Delete cart by cartId
     */
    @Transactional
    fun deleteCart(cartId: Long) {
        logger.info("Deleting cart: $cartId")
        val cart = getCartOrThrow(cartId)

        val userId = cart.user?.id ?: 0L
        cartRepository.delete(cart)
        logger.info("Cart deleted: $cartId")

        // Publish cart deleted event
        publishCartEvent(cartId, userId, "CART_DELETED", "Cart was deleted")
    }

    // ==================== Cart Item Operations ====================

    /**
     * Add item to cart by cartId
     */
    @Transactional
    fun addItemToCart(cartId: Long, request: AddToCartRequest): CartDto {
        logger.info("Adding product ${request.productId} to cart: $cartId")

        val cart = getCartOrThrow(cartId)

        val product = productRepository.findById(request.productId)
            .orElseThrow { NoSuchElementException("Product not found with id: ${request.productId}") }

        val existingItem = cartItemRepository.findByCartIdAndProductId(cartId, request.productId)
        val userId = cart.user?.id ?: 0L

        if (existingItem.isPresent) {
            val item = existingItem.get()
            item.quantity += request.quantity
            cartItemRepository.save(item)
            logger.debug("Updated quantity for product ${request.productId} in cart $cartId")

            // Publish item quantity updated event
            publishCartItemEvent(
                cartId, userId, request.productId, product.name ?: "Unknown",
                item.quantity, "ITEM_QUANTITY_UPDATED", "Item quantity increased"
            )
        } else {
            val newItem = CartItem(cart = cart, product = product, quantity = request.quantity)
            cartItemRepository.save(newItem)
            logger.debug("Added new product ${request.productId} to cart $cartId")

            // Publish item added event
            publishCartItemEvent(
                cartId, userId, request.productId, product.name ?: "Unknown",
                request.quantity, "ITEM_ADDED", "New item added to cart"
            )
        }

        return getUpdatedCart(cartId)
    }

    /**
     * Update item quantity in cart by cartId
     */
    @Transactional
    fun updateItemQuantity(cartId: Long, productId: Long, request: UpdateCartItemRequest): CartDto {
        logger.info("Updating quantity for product $productId in cart: $cartId")

        val cart = getCartOrThrow(cartId)

        val cartItem = getCartItemOrThrow(cartId, productId)
        val userId = cart.user?.id ?: 0L
        val productName = cartItem.product?.name ?: "Unknown"

        cartItem.quantity = request.quantity
        cartItemRepository.save(cartItem)

        logger.info("Item quantity updated in cart: $cartId")

        // Publish item quantity updated event
        publishCartItemEvent(
            cartId, userId, productId, productName,
            request.quantity, "ITEM_QUANTITY_UPDATED", "Item quantity changed to ${request.quantity}"
        )

        return getUpdatedCart(cartId)
    }

    /**
     * Remove item from cart by cartId
     */
    @Transactional
    fun removeItemFromCart(cartId: Long, productId: Long): CartDto {
        logger.info("Removing product $productId from cart: $cartId")

        val cart = getCartOrThrow(cartId)

        val cartItem = getCartItemOrThrow(cartId, productId)
        val userId = cart.user?.id ?: 0L
        val productName = cartItem.product?.name ?: "Unknown"

        cartItemRepository.deleteByCartIdAndProductId(cartId, productId)
        logger.info("Item removed from cart: $cartId")

        // Publish item removed event
        publishCartItemEvent(
            cartId, userId, productId, productName,
            0, "ITEM_REMOVED", "Item removed from cart"
        )

        return getUpdatedCart(cartId)
    }

    // ==================== Cart Status Operations ====================

    /**
     * Update cart status by cartId
     */
    @Transactional
    fun updateCartStatus(cartId: Long, newStatus: CartStatus): CartDto {
        logger.info("Updating cart $cartId status to $newStatus")

        val cart = getCartOrThrow(cartId)

        cart.status = newStatus
        val updatedCart = cartRepository.save(cart)

        logger.info("Cart $cartId status updated to $newStatus")
        return cartMapper.toDto(updatedCart)
    }

    // ==================== Private Helper Methods ====================

    /**
     * Get updated cart with items
     */
    private fun getUpdatedCart(cartId: Long): CartDto {
        val cart = getCartWithItemsOrThrow(cartId)
        return cartMapper.toDto(cart)
    }

    /**
     * Publish cart-level Kafka event
     */
    private fun publishCartEvent(cartId: Long, userId: Long, eventType: String, message: String) {
        try {
            kafkaProducerService.publishOrderEvent(
                OrderEvent(
                    eventType = eventType,
                    userId = userId,
                    cartId = cartId,
                    action = OrderAction.CREATED,
                    totalAmount = null,
                    details = mapOf(
                        "message" to message,
                        "cartId" to cartId
                    )
                )
            )
            logger.debug("Published $eventType event for cart: $cartId")
        } catch (e: Exception) {
            logger.warn("Failed to publish $eventType event: ${e.message}")
        }
    }

    /**
     * Publish cart item-level Kafka event
     */
    private fun publishCartItemEvent(
        cartId: Long,
        userId: Long,
        productId: Long,
        productName: String,
        quantity: Int,
        eventType: String,
        message: String
    ) {
        try {
            kafkaProducerService.publishOrderEvent(
                OrderEvent(
                    eventType = eventType,
                    userId = userId,
                    cartId = cartId,
                    action = OrderAction.CREATED,
                    totalAmount = null,
                    details = mapOf(
                        "message" to message,
                        "productId" to productId,
                        "productName" to productName,
                        "quantity" to quantity
                    )
                )
            )
            logger.debug("Published $eventType event for product $productId in cart: $cartId")
        } catch (e: Exception) {
            logger.warn("Failed to publish $eventType event: ${e.message}")
        }
    }

    /**
     * Checkout cart - updates status and publishes event
     */
    @Transactional
    fun checkoutCart(cartId: Long): CartDto {
        logger.info("Starting checkout for cart: $cartId")

        val cart = getCartWithItemsOrThrow(cartId)

        if (cart.items.isEmpty()) {
            throw IllegalStateException("Cannot checkout an empty cart")
        }

        val userId = cart.user?.id ?: 0L
        cart.status = CartStatus.CHECKOUT
        val updatedCart = cartRepository.save(cart)

        // Calculate total
        val total = cart.items.sumOf {
            (it.product?.price ?: BigDecimal.ZERO) * BigDecimal(it.quantity)
        }

        // Publish checkout started event
        try {
            kafkaProducerService.publishOrderEvent(
                OrderEvent(
                    eventType = "CHECKOUT_STARTED",
                    userId = userId,
                    cartId = cartId,
                    action = OrderAction.CREATED,
                    totalAmount = total,
                    details = mapOf(
                        "itemCount" to cart.items.size,
                        "totalAmount" to total.toString()
                    )
                )
            )
        } catch (e: Exception) {
            logger.warn("Failed to publish checkout event: ${e.message}")
        }

        logger.info("Checkout started for cart: $cartId with total: $total")
        return cartMapper.toDto(updatedCart)
    }

    /**
     * Complete checkout after successful payment
     */
    @Transactional
    fun completeCheckout(cartId: Long, paymentId: String): CartDto {
        logger.info("Completing checkout for cart: $cartId with payment: $paymentId")

        val cart = getCartOrThrow(cartId)

        val userId = cart.user?.id ?: 0L
        cart.status = CartStatus.COMPLETED
        val updatedCart = cartRepository.save(cart)

        // Publish order completed event
        try {
            kafkaProducerService.publishOrderEvent(
                OrderEvent(
                    eventType = "ORDER_COMPLETED",
                    userId = userId,
                    cartId = cartId,
                    action = OrderAction.CONFIRMED,
                    totalAmount = null,
                    details = mapOf(
                        "paymentId" to paymentId,
                        "status" to "COMPLETED"
                    )
                )
            )
        } catch (e: Exception) {
            logger.warn("Failed to publish order completed event: ${e.message}")
        }

        logger.info("Checkout completed for cart: $cartId")
        return cartMapper.toDto(updatedCart)
    }

    /**
     * Helper to fetch cart by id or throw
     */
    private fun getCartOrThrow(cartId: Long): Cart =
        cartRepository.findById(cartId).orElseThrow { NoSuchElementException("Cart not found with id: $cartId") }

    /**
     * Helper to fetch cart with items by id or throw
     */
    private fun getCartWithItemsOrThrow(cartId: Long): Cart =
        cartRepository.findByIdWithItems(cartId).orElseThrow { NoSuchElementException("Cart not found with id: $cartId") }

    /**
     * Helper to fetch cart item by cartId and productId or throw
     */
    private fun getCartItemOrThrow(cartId: Long, productId: Long): CartItem =
        cartItemRepository.findByCartIdAndProductId(cartId, productId)
            .orElseThrow { NoSuchElementException("Product $productId not found in cart $cartId") }

    /**
     * Helper to save cart and return DTO
     */
    private fun saveCartAndReturnDto(cart: Cart): CartDto = cartMapper.toDto(cartRepository.save(cart))
}