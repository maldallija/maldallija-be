package dev.maldallija.maldallijabe.season.enrollment.application.port.`in`

import dev.maldallija.maldallijabe.season.enrollment.application.port.`in`.dto.SeasonEnrollmentDetail
import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface GetSeasonEnrollmentsUseCase {
    fun getSeasonEnrollments(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        requestingUserId: Long,
        enrollmentStatus: EnrollmentStatus?,
        pageable: Pageable,
    ): Page<SeasonEnrollmentDetail>
}
