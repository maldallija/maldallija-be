package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.invitation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(description = "승마장 직원 초대 요청")
data class CreateEquestrianCenterInvitationRequest(
    @field:NotNull
    @Schema(description = "초대할 사용자 UUID")
    val userUuid: UUID,
)
