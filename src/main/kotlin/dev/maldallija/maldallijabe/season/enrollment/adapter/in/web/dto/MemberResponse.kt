package dev.maldallija.maldallijabe.season.enrollment.adapter.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "회원")
data class MemberResponse(
    @Schema(description = "회원 UUID")
    val memberUuid: UUID,
    @Schema(description = "회원 닉네임")
    val memberNickname: String,
)
