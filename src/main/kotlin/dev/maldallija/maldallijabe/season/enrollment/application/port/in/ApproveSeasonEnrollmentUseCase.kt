package dev.maldallija.maldallijabe.season.enrollment.application.port.`in`

import java.util.UUID

interface ApproveSeasonEnrollmentUseCase {
    fun approveSeasonEnrollment(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        enrollmentUuid: UUID,
        requestingUserId: Long,
    )
}
