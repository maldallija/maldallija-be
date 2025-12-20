package dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.dto

import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import java.time.Instant
import java.util.UUID

data class UserEquestrianCenterInvitationDetail(
    val invitationUuid: UUID,
    val equestrianCenterUuid: UUID,
    val equestrianCenterName: String,
    val invitationStatus: InvitationStatus,
    val invitedAt: Instant,
    val expiresAt: Instant,
    val respondedAt: Instant?,
)
