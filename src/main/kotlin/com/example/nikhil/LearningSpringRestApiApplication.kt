package com.example.nikhil

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Spring Boot Application Entry Point
 *
 * Architecture: Clean Architecture with the following layers:
 * - application/usecase: Business logic and use cases (services)
 * - infrastructure/persistence: Database entities and repositories
 * - infrastructure/web: REST controllers, DTOs, and exception handling
 * - infrastructure/security: JWT authentication and security configuration
 * - infrastructure/mapper: Entity-DTO mapping utilities
 * - config: Application-wide configurations
 */
@SpringBootApplication
class LearningSpringRestApiApplication

fun main(args: Array<String>) {
    runApplication<LearningSpringRestApiApplication>(*args)
}
