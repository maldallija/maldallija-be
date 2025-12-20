package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.invitation.dto

import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "초대 목록 응답")
data class EquestrianCenterInvitationListResponse(
    @Schema(description = "초대 UUID")
    val invitationUuid: UUID,
    @Schema(description = "초대받은 사용자 정보")
    val invitedUser: InvitedUserResponse,
    @Schema(description = "초대 상태")
    val status: InvitationStatus,
    @Schema(description = "초대일시")
    val invitedAt: Instant,
    @Schema(description = "만료일시")
    val expiresAt: Instant,
    @Schema(description = "응답 시각 (승인/거절 시)")
    val respondedAt: Instant?,
)
