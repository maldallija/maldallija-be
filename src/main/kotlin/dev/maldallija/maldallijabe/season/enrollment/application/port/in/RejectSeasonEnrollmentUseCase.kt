package dev.maldallija.maldallijabe.season.enrollment.application.port.`in`

import java.util.UUID

interface RejectSeasonEnrollmentUseCase {
    fun rejectSeasonEnrollment(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        enrollmentUuid: UUID,
        requestingUserId: Long,
        note: String?,
    )
}
