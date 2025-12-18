package dev.maldallija.maldallijabe.auth.adapter.`in`.filter

import com.fasterxml.jackson.databind.ObjectMapper
import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AdministratorAuthorizationFilter(
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!request.requestURI.startsWith("/api/v1/administration")) {
            filterChain.doFilter(request, response)
            return
        }

        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication?.principal as? Long

        if (userId == null) {
            sendForbidden(response, "Authentication required")
            return
        }

        val user = userRepository.findById(userId)
        if (user == null || !user.isSystemAdmin) {
            sendForbidden(response, "Insufficient permissions to access this resource")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun sendForbidden(
        response: HttpServletResponse,
        message: String,
    ) {
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        val errorResponse = ErrorResponse(code = "INSUFFICIENT_PERMISSIONS", message = message)
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
