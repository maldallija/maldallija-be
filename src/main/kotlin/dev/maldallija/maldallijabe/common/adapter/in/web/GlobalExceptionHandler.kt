package dev.maldallija.maldallijabe.common.adapter.`in`.web

import dev.maldallija.maldallijabe.auth.domain.exception.AuthException
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.EquestrianCenterException
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.UnauthorizedEquestrianCenterOperationException
import dev.maldallija.maldallijabe.user.domain.exception.UserException
import dev.maldallija.maldallijabe.user.domain.exception.UserNotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                code = e.errorCode,
                message = e.message ?: "User not found",
            )
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse)
    }

    @ExceptionHandler(UserException::class)
    fun handleUserException(e: UserException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                code = e.errorCode,
                message = e.message ?: "User error",
            )
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(errorResponse)
    }

    @ExceptionHandler(AuthException::class)
    fun handleAuthException(e: AuthException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                code = e.errorCode,
                message = e.message ?: "Authentication error",
            )
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(errorResponse)
    }

    @ExceptionHandler(UnauthorizedEquestrianCenterOperationException::class)
    fun handleUnauthorizedEquestrianCenterOperationException(
        e: UnauthorizedEquestrianCenterOperationException,
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                code = e.errorCode,
                message = e.message ?: "Forbidden",
            )
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(errorResponse)
    }

    @ExceptionHandler(EquestrianCenterNotFoundException::class)
    fun handleEquestrianCenterNotFoundException(e: EquestrianCenterNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                code = e.errorCode,
                message = e.message ?: "Equestrian center not found",
            )
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse)
    }

    @ExceptionHandler(EquestrianCenterException::class)
    fun handleEquestrianCenterException(e: EquestrianCenterException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                code = e.errorCode,
                message = e.message ?: "Equestrian center error",
            )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(e: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                code = "DUPLICATE_USERNAME",
                message = "Username already exists",
            )
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                code = "INVALID_REQUEST",
                message = e.message ?: "Invalid request",
            )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                code = "INTERNAL_SERVER_ERROR",
                message = "An unexpected error occurred",
            )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse)
    }
}
