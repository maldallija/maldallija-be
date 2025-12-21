package dev.maldallija.maldallijabe.equestriancenter.staff.adapter.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class StaffUserResponse(
    @Schema(description = "사용자 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    val uuid: UUID,
    @Schema(description = "사용자 닉네임", example = "홍길동")
    val nickname: String,
)
