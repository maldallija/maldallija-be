package dev.maldallija.maldallijabe.season.enrollmentlog.adapter.out.persistence

import dev.maldallija.maldallijabe.season.enrollmentlog.domain.EnrollmentLogType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "season_enrollment_log")
class SeasonEnrollmentLogEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(name = "season_enrollment_id", nullable = false)
    val seasonEnrollmentId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val enrollmentLogType: EnrollmentLogType,
    @Column(name = "actor_id")
    val actorId: Long?,
    @Column(name = "note", columnDefinition = "TEXT")
    val note: String?,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
