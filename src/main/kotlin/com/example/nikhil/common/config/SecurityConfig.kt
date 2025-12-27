package com.example.nikhil.common.config

import com.example.nikhil.auth.util.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Security Configuration
 * Configures JWT-based stateless authentication with role-based authorization
 *
 * Roles:
 * - ROLE_CUSTOMER: Default role for regular users (can browse, buy, manage own orders)
 * - ROLE_ADMIN: Admin role (full access to all resources)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    companion object {
        // Public endpoints - no authentication required
        private val PUBLIC_ENDPOINTS = arrayOf(
            "/",
            "/api/message",
            "/auth/**",
            "/actuator/health",
            "/api-console.html",
            "/static/**",
            "/*.html",
            "/*.css",
            "/*.js",
            // Swagger/OpenAPI endpoints
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
        )

        // Endpoints accessible by any authenticated user
        private val AUTHENTICATED_ENDPOINTS = arrayOf(
            "/users/me",
            "/auth/me"
        )

        // Endpoints accessible by CUSTOMER and ADMIN
        private val CUSTOMER_ENDPOINTS = arrayOf(
            "/carts/**",
            "/orders/checkout",
            "/orders/my-orders",
            "/payments/**"
        )

        // Endpoints accessible only by ADMIN
        private val ADMIN_ENDPOINTS = arrayOf(
            "/admin/**",
            "/users/*/roles/**"
        )
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints - no auth required
                    .requestMatchers(*PUBLIC_ENDPOINTS).permitAll()

                    // User registration - public
                    .requestMatchers(HttpMethod.POST, "/users").permitAll()
                    .requestMatchers(HttpMethod.POST, "/users/login").permitAll()

                    // Products - public read access
                    .requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()

                    // Products - admin only for create/update/delete
                    .requestMatchers(HttpMethod.POST, "/products", "/products/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")

                    // Categories - public read, admin write
                    .requestMatchers(HttpMethod.GET, "/categories", "/categories/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/categories/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN")

                    // Orders - admin can view all, customers can only view their own
                    .requestMatchers(HttpMethod.GET, "/orders/user/**").hasAnyRole("CUSTOMER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/orders/*/status").hasRole("ADMIN")
                    .requestMatchers("/orders/**").hasAnyRole("CUSTOMER", "ADMIN")

                    // Carts and Payments - customer and admin
                    .requestMatchers("/carts/**").hasAnyRole("CUSTOMER", "ADMIN")
                    .requestMatchers("/payments/**").hasAnyRole("CUSTOMER", "ADMIN")

                    // User management - admin only for viewing all users
                    .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")

                    // User self-management
                    .requestMatchers("/users/me", "/auth/me").authenticated()
                    .requestMatchers(HttpMethod.GET, "/users/{id}").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/users/{id}").authenticated()
                    .requestMatchers("/users/*/change-password").authenticated()

                    // Admin endpoints
                    .requestMatchers(*ADMIN_ENDPOINTS).hasRole("ADMIN")

                    // Test endpoints (remove in production)
                    .requestMatchers("/test/**").permitAll()

                    // All other requests require authentication
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}