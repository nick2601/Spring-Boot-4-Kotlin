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
 * Supports automatic token refresh when token is about to expire
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
        const val NEW_ACCESS_TOKEN_HEADER = "X-New-Access-Token"
        const val TOKEN_REFRESH_HEADER = "X-Token-Refreshed"
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

                    // Check if token is valid (and is an access token)
                    if (jwtTokenUtil.validateToken(token, userDetails.username) &&
                        jwtTokenUtil.isAccessToken(token)) {

                        val authToken = UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.authorities
                        ).apply {
                            details = WebAuthenticationDetailsSource().buildDetails(request)
                        }
                        SecurityContextHolder.getContext().authentication = authToken
                        log.debug("Authentication successful for user: $email")

                        // Check if token needs refresh (approaching expiry)
                        if (jwtTokenUtil.needsRefresh(token)) {
                            val userId = jwtTokenUtil.getUserIdFromToken(token)
                            val name = jwtTokenUtil.getNameFromToken(token)
                            val roles = jwtTokenUtil.getRolesFromToken(token)

                            // Generate new access token
                            val newAccessToken = jwtTokenUtil.generateAccessToken(
                                email, userId, name, roles
                            )

                            // Add new token to response header
                            response.setHeader(NEW_ACCESS_TOKEN_HEADER, newAccessToken)
                            response.setHeader(TOKEN_REFRESH_HEADER, "true")

                            log.info("Auto-refreshed access token for user: $email")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            log.error("Cannot set user authentication: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }
}

