package dev.maldallija.maldallijabe.auth.adapter.`in`.filter

import com.fasterxml.jackson.databind.ObjectMapper
import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

/**
 * 인증 실패 시 401 Unauthorized 응답을 반환하는 EntryPoint
 */
@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"

        val errorResponse =
            ErrorResponse(
                code = "AUTHENTICATION_FAILED",
                message = authException.message ?: "Authentication failed",
            )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
