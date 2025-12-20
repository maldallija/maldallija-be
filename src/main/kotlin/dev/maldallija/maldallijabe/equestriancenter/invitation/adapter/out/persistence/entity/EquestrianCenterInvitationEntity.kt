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
    var id: Long = 0L,
    @Column(nullable = false, unique = true)
    var uuid: UUID,
    @Column(nullable = false, name = "equestrian_center_id")
    var equestrianCenterId: Long,
    @Column(nullable = false, name = "user_id")
    var userId: Long,
    @Column(nullable = false, name = "invited_by")
    var invitedBy: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: InvitationStatus,
    @Column(nullable = false, name = "invited_at")
    var invitedAt: Instant,
    @Column(name = "responded_at")
    var respondedAt: Instant?,
    @Column(nullable = false, name = "expires_at")
    var expiresAt: Instant,
    @Column(nullable = false, name = "created_at")
    var createdAt: Instant,
    @Column(nullable = false, name = "updated_at")
    var updatedAt: Instant,
    @Column(nullable = false, name = "updated_by")
    var updatedBy: Long,
)
