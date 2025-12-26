package com.example.nikhil.infrastructure.web.controller

import com.example.nikhil.application.service.OrderService
import com.example.nikhil.infrastructure.persistence.entity.OrderStatus
import com.example.nikhil.infrastructure.web.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

/**
 * Order Controller
 * REST endpoints for order management and order history
 */
@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management and order history endpoints")
class OrderController(
    private val orderService: OrderService
) {

    // ==================== Order Creation ====================

    @PostMapping("/checkout")
    @Operation(
        summary = "Create order from cart",
        description = "Checkout cart and create a new order"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Order created successfully",
                content = [Content(schema = Schema(implementation = OrderDto::class))]
            ),
            ApiResponse(responseCode = "400", description = "Invalid cart or empty cart", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Cart not found", content = [Content()])
        ]
    )
    fun createOrder(
        @Valid @RequestBody request: CreateOrderRequest
    ): ResponseEntity<OrderDto> {
        val order = orderService.createOrderFromCart(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(order)
    }

    @PostMapping("/{orderId}/complete")
    @Operation(
        summary = "Complete order after payment",
        description = "Mark order as paid after successful payment"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Order completed"),
            ApiResponse(responseCode = "404", description = "Order not found", content = [Content()])
        ]
    )
    fun completeOrder(
        @Parameter(description = "Order ID") @PathVariable orderId: Long,
        @RequestParam paymentId: String
    ): ResponseEntity<OrderDto> {
        val order = orderService.completeOrder(orderId, paymentId)
        return ResponseEntity.ok(order)
    }

    // ==================== Order Retrieval ====================

    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get order by ID",
        description = "Retrieve order details by order ID"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Order retrieved",
                content = [Content(schema = Schema(implementation = OrderDto::class))]
            ),
            ApiResponse(responseCode = "404", description = "Order not found", content = [Content()])
        ]
    )
    fun getOrderById(
        @Parameter(description = "Order ID") @PathVariable orderId: Long
    ): ResponseEntity<OrderDto> {
        val order = orderService.getOrderById(orderId)
        return ResponseEntity.ok(order)
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(
        summary = "Get order by order number",
        description = "Retrieve order details by order number"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Order retrieved"),
            ApiResponse(responseCode = "404", description = "Order not found", content = [Content()])
        ]
    )
    fun getOrderByNumber(
        @Parameter(description = "Order Number") @PathVariable orderNumber: String
    ): ResponseEntity<OrderDto> {
        val order = orderService.getOrderByNumber(orderNumber)
        return ResponseEntity.ok(order)
    }

    // ==================== Order History ====================

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get order history for user",
        description = "Retrieve paginated order history for a specific user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Order history retrieved",
                content = [Content(schema = Schema(implementation = OrderHistoryResponse::class))]
            )
        ]
    )
    fun getOrderHistory(
        @Parameter(description = "User ID") @PathVariable userId: Long,
        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<OrderHistoryResponse> {
        val history = orderService.getOrderHistory(userId, page, size)
        return ResponseEntity.ok(history)
    }

    @GetMapping("/my-orders")
    @Operation(
        summary = "Get current user's order history",
        description = "Retrieve order history for the authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Order history retrieved"),
            ApiResponse(responseCode = "401", description = "Not authenticated", content = [Content()])
        ]
    )
    fun getMyOrders(
        authentication: Authentication,
        @Parameter(description = "Page number") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<OrderHistoryResponse> {
        // Get user ID from authentication (you'll need to implement this based on your user lookup)
        val email = authentication.name
        // For now, return a placeholder - you should get userId from email
        return ResponseEntity.ok(
            OrderHistoryResponse(
                orders = emptyList(),
                totalOrders = 0,
                page = page,
                pageSize = size,
                totalPages = 0
            )
        )
    }

    @GetMapping("/user/{userId}/all")
    @Operation(
        summary = "Get all orders for user",
        description = "Retrieve all orders for a user without pagination"
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getAllOrdersForUser(
        @Parameter(description = "User ID") @PathVariable userId: Long
    ): ResponseEntity<List<OrderSummaryDto>> {
        val orders = orderService.getAllOrdersForUser(userId)
        return ResponseEntity.ok(orders)
    }

    @GetMapping("/user/{userId}/status/{status}")
    @Operation(
        summary = "Get orders by status",
        description = "Retrieve orders for a user filtered by status"
    )
    @SecurityRequirement(name = "bearerAuth")
    fun getOrdersByStatus(
        @Parameter(description = "User ID") @PathVariable userId: Long,
        @Parameter(description = "Order status") @PathVariable status: String
    ): ResponseEntity<List<OrderSummaryDto>> {
        val orderStatus = try {
            OrderStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
        val orders = orderService.getOrdersByStatus(userId, orderStatus)
        return ResponseEntity.ok(orders)
    }

    // ==================== Order Status Management ====================

    @PutMapping("/{orderId}/status")
    @Operation(
        summary = "Update order status",
        description = "Update the status of an order (admin operation)"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Status updated"),
            ApiResponse(responseCode = "400", description = "Invalid status", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Order not found", content = [Content()])
        ]
    )
    fun updateOrderStatus(
        @Parameter(description = "Order ID") @PathVariable orderId: Long,
        @Valid @RequestBody request: UpdateOrderStatusRequest
    ): ResponseEntity<OrderDto> {
        val order = orderService.updateOrderStatus(orderId, request)
        return ResponseEntity.ok(order)
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(
        summary = "Cancel order",
        description = "Cancel an order that hasn't been shipped"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Order cancelled"),
            ApiResponse(responseCode = "400", description = "Cannot cancel order", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Order not found", content = [Content()])
        ]
    )
    fun cancelOrder(
        @Parameter(description = "Order ID") @PathVariable orderId: Long,
        @RequestParam(required = false) reason: String?
    ): ResponseEntity<OrderDto> {
        val order = orderService.cancelOrder(orderId, reason)
        return ResponseEntity.ok(order)
    }
}

