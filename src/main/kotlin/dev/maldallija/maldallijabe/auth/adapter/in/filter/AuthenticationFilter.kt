package dev.maldallija.maldallijabe.auth.adapter.`in`.filter

import dev.maldallija.maldallijabe.auth.adapter.`in`.web.auth.constant.AuthenticationSessionCookieName
import dev.maldallija.maldallijabe.auth.application.port.`in`.ValidateAuthenticationSessionUseCase
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class AuthenticationFilter(
    private val validateAuthenticationSessionUseCase: ValidateAuthenticationSessionUseCase,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authenticationAccessSessionCookie =
            request.cookies?.find { it.name == AuthenticationSessionCookieName.ACCESS_SESSION }?.value
                ?: throw SimpleAuthenticationException("Missing authentication session")

        val authenticationAccessSessionId =
            try {
                UUID.fromString(authenticationAccessSessionCookie)
            } catch (e: IllegalArgumentException) {
                throw SimpleAuthenticationException("Invalid authentication session format", e)
            }

        val userId =
            try {
                validateAuthenticationSessionUseCase.validateAuthenticationSession(authenticationAccessSessionId)
            } catch (e: Exception) {
                throw SimpleAuthenticationException("Authentication session validation failed", e)
            }

        val authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }

    private class SimpleAuthenticationException(
        message: String,
        cause: Throwable? = null,
    ) : AuthenticationException(message, cause)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/api/v1/auth/sign-in") ||
            path.startsWith("/api/v1/auth/sign-up") ||
            path.startsWith("/api/v1/auth/sessions/refresh") ||
            (path.startsWith("/api/v1/equestrian-centers") && request.method == HttpMethod.GET.name()) ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs")
    }
}
