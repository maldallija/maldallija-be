package dev.maldallija.maldallijabe.auth.adapter.`in`.web.auth.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원가입 요청")
data class SignUpRequest(
    val username: String,
    val password: String,
    val nickname: String,
)
