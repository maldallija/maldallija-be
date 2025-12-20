package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.invitation.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "초대받은 사용자")
data class InvitedUserResponse(
    @Schema(description = "사용자 UUID")
    val uuid: UUID,
    @Schema(description = "닉네임")
    val nickname: String,
)
