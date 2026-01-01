package dev.maldallija.maldallijabe.season.enrollment.adapter.out.persistence

import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
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
@Table(name = "season_enrollment")
class SeasonEnrollmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(name = "uuid", nullable = false, unique = true)
    val uuid: UUID,
    @Column(name = "season_id", nullable = false)
    val seasonId: Long,
    @Column(name = "member_id", nullable = false)
    val memberId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val enrollmentStatus: EnrollmentStatus,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
)
