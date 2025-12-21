package dev.maldallija.maldallijabe.auth.adapter.`in`.filter

import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AdministratorAuthorizationFilter(
    private val userRepository: UserRepository,
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
        val userId =
            authentication?.principal as? Long
                ?: throw AccessDeniedException("Insufficient permissions")

        val user = userRepository.findById(userId)
        if (user == null || !user.isSystemAdmin) {
            throw AccessDeniedException("Insufficient permissions")
        }

        filterChain.doFilter(request, response)
    }
}
