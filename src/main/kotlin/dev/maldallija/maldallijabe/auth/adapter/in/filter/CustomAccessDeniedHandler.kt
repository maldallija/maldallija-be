package dev.maldallija.maldallijabe.auth.adapter.`in`.filter

import com.fasterxml.jackson.databind.ObjectMapper
import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

/**
 * 인가 실패 시 403 Forbidden 응답을 반환하는 Handler
 */
@Component
class CustomAccessDeniedHandler(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"

        val errorResponse =
            ErrorResponse(
                code = "INSUFFICIENT_PERMISSIONS",
                message = accessDeniedException.message ?: "Insufficient permissions",
            )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
