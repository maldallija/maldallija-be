package dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "equestrian_center_staff")
class EquestrianCenterStaffEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    @Column(nullable = false, unique = true)
    var uuid: UUID,
    @Column(nullable = false, name = "equestrian_center_id")
    var equestrianCenterId: Long,
    @Column(nullable = false, name = "user_id")
    var userId: Long,
    @Column(nullable = false, name = "joined_at")
    var joinedAt: Instant,
    @Column(name = "left_at")
    var leftAt: Instant?,
    @Column(name = "left_by")
    var leftBy: Long?,
    @Column(name = "left_reason")
    var leftReason: String?,
    @Column(nullable = false, name = "created_at")
    var createdAt: Instant,
    @Column(nullable = false, name = "updated_at")
    var updatedAt: Instant,
    @Column(nullable = false, name = "updated_by")
    var updatedBy: Long,
    @Column(name = "deleted_at")
    var deletedAt: Instant?,
)
