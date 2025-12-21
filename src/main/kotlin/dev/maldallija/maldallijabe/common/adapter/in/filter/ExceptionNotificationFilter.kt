package dev.maldallija.maldallijabe.common.adapter.`in`.filter

import dev.maldallija.maldallijabe.common.config.Environment
import dev.maldallija.maldallijabe.notification.application.service.ExceptionNotificationService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Profile(Environment.PRODUCTION)
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class ExceptionNotificationFilter(
    private val notificationService: ExceptionNotificationService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        filterChain.doFilter(request, response)

        if (response.status >= 500) {
            notificationService.notifyException(request, response)
        }
    }
}
