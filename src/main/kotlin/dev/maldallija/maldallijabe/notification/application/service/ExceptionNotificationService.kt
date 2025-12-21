package dev.maldallija.maldallijabe.notification.application.service

import dev.maldallija.maldallijabe.common.config.Environment
import dev.maldallija.maldallijabe.common.domain.ExceptionEvent
import dev.maldallija.maldallijabe.notification.application.port.out.NotificationPort
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Instant

@Profile(Environment.PRODUCTION)
@Service
class ExceptionNotificationService(
    private val notificationPort: NotificationPort,
) {
    fun notifyException(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val event =
            ExceptionEvent(
                timestamp = Instant.now(),
                statusCode = response.status,
                method = request.method,
                path = request.requestURI,
                errorMessage = response.getHeader("X-Error-Message"),
                userAgent = request.getHeader("User-Agent"),
            )

        notificationPort.notifyException(event)
    }
}
