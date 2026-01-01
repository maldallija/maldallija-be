package dev.maldallija.maldallijabe.season.enrollmentlog.domain

import java.time.Instant

data class SeasonEnrollmentLog(
    val id: Long,
    val seasonEnrollmentId: Long,
    val enrollmentLogType: EnrollmentLogType,
    val actorId: Long?,
    val note: String?,
    val createdAt: Instant,
)
