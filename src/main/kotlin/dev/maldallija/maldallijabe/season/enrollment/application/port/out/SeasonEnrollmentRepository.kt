package dev.maldallija.maldallijabe.season.enrollment.application.port.out

import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
import dev.maldallija.maldallijabe.season.enrollment.domain.SeasonEnrollment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface SeasonEnrollmentRepository {
    fun save(seasonEnrollment: SeasonEnrollment): SeasonEnrollment

    fun findByUuid(uuid: UUID): SeasonEnrollment?

    fun findBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): SeasonEnrollment?

    fun findBySeasonIdAndMemberIdAndStatus(
        seasonId: Long,
        memberId: Long,
        enrollmentStatus: EnrollmentStatus,
    ): SeasonEnrollment?

    fun countBySeasonIdAndStatus(
        seasonId: Long,
        enrollmentStatus: EnrollmentStatus,
    ): Long

    fun existsBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): Boolean

    fun findBySeasonIdAndOptionalStatus(
        seasonId: Long,
        enrollmentStatus: EnrollmentStatus?,
        pageable: Pageable,
    ): Page<SeasonEnrollment>
}
