package com.example.nikhil.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT Authentication Filter
 * Validates JWT tokens on each request and sets up Spring Security context
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenUtil: JwtTokenUtil,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val AUTHORIZATION_HEADER = "Authorization"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader(AUTHORIZATION_HEADER)

            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                val token = authHeader.substring(BEARER_PREFIX.length)
                val email = jwtTokenUtil.getEmailFromToken(token)

                if (email != null && SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(email)

                    if (jwtTokenUtil.validateToken(token, userDetails.username)) {
                        val authToken = UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.authorities
                        ).apply {
                            details = WebAuthenticationDetailsSource().buildDetails(request)
                        }
                        SecurityContextHolder.getContext().authentication = authToken
                        log.debug("Authentication successful for user: $email")
                    }
                }
            }
        } catch (e: Exception) {
            log.error("Cannot set user authentication: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }
}

