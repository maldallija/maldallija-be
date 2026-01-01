package dev.maldallija.maldallijabe.season.enrollment.adapter.out.persistence

import dev.maldallija.maldallijabe.season.enrollment.domain.SeasonEnrollment
import org.springframework.stereotype.Component

@Component
class SeasonEnrollmentMapper {
    fun toDomain(entity: SeasonEnrollmentEntity): SeasonEnrollment =
        SeasonEnrollment(
            id = entity.id,
            uuid = entity.uuid,
            seasonId = entity.seasonId,
            memberId = entity.memberId,
            enrollmentStatus = entity.enrollmentStatus,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(domain: SeasonEnrollment): SeasonEnrollmentEntity =
        SeasonEnrollmentEntity(
            id = domain.id,
            uuid = domain.uuid,
            seasonId = domain.seasonId,
            memberId = domain.memberId,
            enrollmentStatus = domain.enrollmentStatus,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
        )
}
