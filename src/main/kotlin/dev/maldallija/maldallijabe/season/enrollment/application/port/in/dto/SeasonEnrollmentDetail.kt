package dev.maldallija.maldallijabe.season.enrollment.application.port.`in`.dto

import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
import java.time.Instant
import java.util.UUID

data class SeasonEnrollmentDetail(
    val enrollmentUuid: UUID,
    val memberUuid: UUID,
    val memberNickname: String,
    val enrollmentStatus: EnrollmentStatus,
    val createdAt: Instant,
)
