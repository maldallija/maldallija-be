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
    val id: Long = 0L,
    @Column(nullable = false, unique = true)
    val uuid: UUID,
    @Column(nullable = false, name = "equestrian_center_id")
    val equestrianCenterId: Long,
    @Column(nullable = false, name = "user_id")
    val userId: Long,
    @Column(nullable = false, name = "joined_at")
    val joinedAt: Instant,
    @Column(name = "left_at")
    val leftAt: Instant?,
    @Column(name = "left_by")
    val leftBy: Long?,
    @Column(name = "left_reason")
    val leftReason: String?,
    @Column(nullable = false, name = "created_at")
    val createdAt: Instant,
    @Column(nullable = false, name = "updated_at")
    val updatedAt: Instant,
    @Column(nullable = false, name = "updated_by")
    val updatedBy: Long,
    @Column(name = "deleted_at")
    val deletedAt: Instant?,
)
