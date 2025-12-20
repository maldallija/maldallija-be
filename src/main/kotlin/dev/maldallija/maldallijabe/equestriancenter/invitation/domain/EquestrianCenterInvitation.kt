package dev.maldallija.maldallijabe.equestriancenter.invitation.domain

import java.time.Instant
import java.util.UUID

data class EquestrianCenterInvitation(
    val id: Long,
    val uuid: UUID,
    val equestrianCenterId: Long,
    val userId: Long,
    val invitedBy: Long,
    val status: InvitationStatus,
    val invitedAt: Instant,
    val respondedAt: Instant?,
    val expiresAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
    val updatedBy: Long,
)
