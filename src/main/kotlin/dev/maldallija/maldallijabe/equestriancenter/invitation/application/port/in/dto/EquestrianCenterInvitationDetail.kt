package dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.dto

import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import java.time.Instant
import java.util.UUID

data class EquestrianCenterInvitationDetail(
    val invitationUuid: UUID,
    val invitedUserUuid: UUID,
    val invitedUserNickname: String,
    val invitationStatus: InvitationStatus,
    val invitedAt: Instant,
    val expiresAt: Instant,
    val respondedAt: Instant?,
)
