package com.example.nikhil.auth

import com.example.nikhil.auth.dto.MessageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Home Controller
 * Handles root and health check endpoints
 */
@RestController
@Tag(name = "Home", description = "Root and health check endpoints")
class HomeController {

    @GetMapping("/")
    @Operation(summary = "Welcome message", description = "Returns a welcome message from the API")
    fun index(): MessageResponse = MessageResponse("Welcome to the Spring Boot REST API!")

    @GetMapping("/api/message")
    @Operation(summary = "Get message", description = "Returns a hello message from the API")
    fun getMessage(): MessageResponse = MessageResponse("Hello from REST API!")
}