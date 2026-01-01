package dev.maldallija.maldallijabe.season.enrollment.domain

import java.time.Instant
import java.util.UUID

data class SeasonEnrollment(
    val id: Long,
    val uuid: UUID,
    val seasonId: Long,
    val memberId: Long,
    val enrollmentStatus: EnrollmentStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)
