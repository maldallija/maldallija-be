package dev.maldallija.maldallijabe.equestriancenter.invitation.adapter.out.persistence.entity

import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "equestrian_center_invitation")
class EquestrianCenterInvitationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    @Column(nullable = false, unique = true)
    val uuid: UUID,
    @Column(nullable = false, name = "equestrian_center_id")
    val equestrianCenterId: Long,
    @Column(nullable = false, name = "user_id")
    val userId: Long,
    @Column(nullable = false, name = "invited_by")
    val invitedBy: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: InvitationStatus,
    @Column(nullable = false, name = "invited_at")
    val invitedAt: Instant,
    @Column(name = "responded_at")
    val respondedAt: Instant?,
    @Column(nullable = false, name = "expires_at")
    val expiresAt: Instant,
    @Column(nullable = false, name = "created_at")
    val createdAt: Instant,
    @Column(nullable = false, name = "updated_at")
    val updatedAt: Instant,
    @Column(nullable = false, name = "updated_by")
    val updatedBy: Long,
)
