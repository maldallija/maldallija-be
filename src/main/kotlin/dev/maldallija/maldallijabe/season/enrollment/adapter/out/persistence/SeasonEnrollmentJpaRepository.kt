package dev.maldallija.maldallijabe.season.enrollment.adapter.out.persistence

import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SeasonEnrollmentJpaRepository : JpaRepository<SeasonEnrollmentEntity, Long> {
    fun findByUuid(uuid: UUID): SeasonEnrollmentEntity?

    fun findBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): SeasonEnrollmentEntity?

    fun findBySeasonIdAndMemberIdAndEnrollmentStatus(
        seasonId: Long,
        memberId: Long,
        enrollmentStatus: EnrollmentStatus,
    ): SeasonEnrollmentEntity?

    fun countBySeasonIdAndEnrollmentStatus(
        seasonId: Long,
        enrollmentStatus: EnrollmentStatus,
    ): Long

    fun existsBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): Boolean
}
