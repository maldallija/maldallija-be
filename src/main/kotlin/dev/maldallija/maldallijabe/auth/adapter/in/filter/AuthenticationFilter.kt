package dev.maldallija.maldallijabe.auth.adapter.`in`.filter

import com.fasterxml.jackson.databind.ObjectMapper
import dev.maldallija.maldallijabe.auth.adapter.`in`.web.constant.AuthenticationSessionCookieName
import dev.maldallija.maldallijabe.auth.application.port.`in`.ValidateSessionUseCase
import dev.maldallija.maldallijabe.auth.domain.exception.InvalidSessionException
import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class AuthenticationFilter(
    private val validateSessionUseCase: ValidateSessionUseCase,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authenticationAccessSession =
            request.cookies?.find { it.name == AuthenticationSessionCookieName.ACCESS_SESSION }?.value

        if (authenticationAccessSession == null) {
            sendUnauthorized(response, "Missing authentication access session")
            return
        }

        val userId =
            try {
                val sessionId = UUID.fromString(authenticationAccessSession)
                validateSessionUseCase.validateSession(sessionId)
            } catch (e: IllegalArgumentException) {
                sendUnauthorized(response, "Invalid authentication access session")
                return
            } catch (e: InvalidSessionException) {
                sendUnauthorized(response, "Invalid or expired session")
                return
            }

        val authentication = UsernamePasswordAuthenticationToken(userId, null, emptyList())
        SecurityContextHolder.getContext().authentication = authentication

        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/api/v1/auth/sign-in") ||
            path.startsWith("/api/v1/auth/sign-up") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs")
    }

    private fun sendUnauthorized(
        response: HttpServletResponse,
        message: String,
    ) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        val errorResponse = ErrorResponse(code = "UNAUTHORIZED", message = message)
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
