package dev.maldallija.maldallijabe.season.enrollment.adapter.out.persistence

import dev.maldallija.maldallijabe.season.enrollment.application.port.out.SeasonEnrollmentRepository
import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
import dev.maldallija.maldallijabe.season.enrollment.domain.SeasonEnrollment
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class SeasonEnrollmentRepositoryAdapter(
    private val seasonEnrollmentJpaRepository: SeasonEnrollmentJpaRepository,
    private val seasonEnrollmentMapper: SeasonEnrollmentMapper,
) : SeasonEnrollmentRepository {
    override fun save(seasonEnrollment: SeasonEnrollment): SeasonEnrollment {
        val entity = seasonEnrollmentMapper.toEntity(seasonEnrollment)
        val savedEntity = seasonEnrollmentJpaRepository.save(entity)
        return seasonEnrollmentMapper.toDomain(savedEntity)
    }

    override fun findByUuid(uuid: UUID): SeasonEnrollment? =
        seasonEnrollmentJpaRepository
            .findByUuid(uuid)
            ?.let { seasonEnrollmentMapper.toDomain(it) }

    override fun findBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): SeasonEnrollment? =
        seasonEnrollmentJpaRepository
            .findBySeasonIdAndMemberId(
                seasonId = seasonId,
                memberId = memberId,
            )?.let { seasonEnrollmentMapper.toDomain(it) }

    override fun findBySeasonIdAndMemberIdAndStatus(
        seasonId: Long,
        memberId: Long,
        enrollmentStatus: EnrollmentStatus,
    ): SeasonEnrollment? =
        seasonEnrollmentJpaRepository
            .findBySeasonIdAndMemberIdAndEnrollmentStatus(
                seasonId = seasonId,
                memberId = memberId,
                enrollmentStatus = enrollmentStatus,
            )?.let { seasonEnrollmentMapper.toDomain(it) }

    override fun countBySeasonIdAndStatus(
        seasonId: Long,
        enrollmentStatus: EnrollmentStatus,
    ): Long =
        seasonEnrollmentJpaRepository.countBySeasonIdAndEnrollmentStatus(
            seasonId = seasonId,
            enrollmentStatus = enrollmentStatus,
        )

    override fun existsBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): Boolean =
        seasonEnrollmentJpaRepository.existsBySeasonIdAndMemberId(
            seasonId = seasonId,
            memberId = memberId,
        )
}
