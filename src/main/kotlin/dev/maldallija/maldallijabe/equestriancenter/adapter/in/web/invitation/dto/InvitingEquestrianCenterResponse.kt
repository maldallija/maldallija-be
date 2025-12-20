package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.invitation.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "초대한 승마장")
data class InvitingEquestrianCenterResponse(
    @Schema(description = "승마장 UUID")
    val uuid: UUID,
    @Schema(description = "승마장 이름")
    val name: String,
)
