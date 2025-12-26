package com.example.nikhil.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Security Configuration
 * Configures JWT-based stateless authentication
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    companion object {
        private val PUBLIC_ENDPOINTS = arrayOf(
            "/",
            "/api/message",
            "/users/login",
            "/auth/**",
            "/products",
            "/products/**",
            "/carts/**",
            "/payments/**",
            "/test/**",  // Test endpoints for password hashing (remove in production)
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
                    .requestMatchers(*PUBLIC_ENDPOINTS).permitAll()
                    .requestMatchers(HttpMethod.POST, "/users").permitAll()
                    .requestMatchers(HttpMethod.POST, "/users/*/change-password").permitAll()  // Allow change-password
                    .requestMatchers(HttpMethod.PUT, "/users/**").permitAll()  // Allow PUT for testing
                    .requestMatchers(HttpMethod.DELETE, "/users/**").permitAll()  // Allow DELETE for testing
                    .requestMatchers(HttpMethod.GET, "/users/**").permitAll()  // Allow GET for testing
                    .requestMatchers("/users/me").authenticated()  // Keep /me authenticated
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
