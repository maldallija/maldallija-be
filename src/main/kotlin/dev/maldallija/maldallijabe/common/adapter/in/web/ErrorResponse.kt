package dev.maldallija.maldallijabe.common.adapter.`in`.web

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "에러 응답")
data class ErrorResponse(
    @Schema(description = "에러 코드", example = "DUPLICATE_USERNAME")
    val code: String,

    @Schema(description = "에러 메시지", example = "Username already exists")
    val message: String,
)
