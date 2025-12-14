package dev.maldallija.maldallijabe.auth.adapter.`in`.web

import dev.maldallija.maldallijabe.auth.adapter.`in`.web.dto.SignInRequest
import dev.maldallija.maldallijabe.auth.adapter.`in`.web.dto.SignUpRequest
import dev.maldallija.maldallijabe.auth.application.port.`in`.SignInUseCase
import dev.maldallija.maldallijabe.auth.application.port.`in`.SignUpUseCase
import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
) {
    @Operation(summary = "로그인")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
            ),
            ApiResponse(
                responseCode = "401",
                description = "잘못된 username 또는 password",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PostMapping("/sign-in")
    fun signIn(
        @RequestBody request: SignInRequest,
    ): ResponseEntity<Unit> {
        val accessSession =
            signInUseCase.signIn(
                username = request.username,
                password = request.password,
            )

        val cookie =
            ResponseCookie
                .from("accessToken", accessSession.accessToken.toString())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build()

        return ResponseEntity
            .ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .build()
    }

    @Operation(summary = "회원가입")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
            ),
            ApiResponse(
                responseCode = "409",
                description = "중복된 username",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PostMapping("/sign-up")
    fun signUp(
        @RequestBody request: SignUpRequest,
    ): ResponseEntity<Unit> {
        signUpUseCase.signUp(
            username = request.username,
            password = request.password,
            nickname = request.nickname,
        )

        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}
