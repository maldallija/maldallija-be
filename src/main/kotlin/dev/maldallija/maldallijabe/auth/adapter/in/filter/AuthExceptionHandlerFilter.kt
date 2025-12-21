package dev.maldallija.maldallijabe.auth.adapter.`in`.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 인증/인가 필터에서 발생하는 예외를 처리하는 필터
 *
 * AuthenticationException → AuthenticationEntryPoint (401)
 * AccessDeniedException → AccessDeniedHandler (403)
 */
@Component
class AuthExceptionHandlerFilter(
    private val authenticationEntryPoint: AuthenticationEntryPoint,
    private val accessDeniedHandler: AccessDeniedHandler,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: AuthenticationException) {
            authenticationEntryPoint.commence(request, response, e)
        } catch (e: AccessDeniedException) {
            accessDeniedHandler.handle(request, response, e)
        }
    }
}
