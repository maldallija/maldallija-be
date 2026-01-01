package dev.maldallija.maldallijabe.season.enrollmentlog.adapter.out.persistence

import dev.maldallija.maldallijabe.season.enrollmentlog.domain.SeasonEnrollmentLog
import org.springframework.stereotype.Component

@Component
class SeasonEnrollmentLogMapper {
    fun toDomain(entity: SeasonEnrollmentLogEntity): SeasonEnrollmentLog =
        SeasonEnrollmentLog(
            id = entity.id,
            seasonEnrollmentId = entity.seasonEnrollmentId,
            enrollmentLogType = entity.enrollmentLogType,
            actorId = entity.actorId,
            note = entity.note,
            createdAt = entity.createdAt,
        )

    fun toEntity(domain: SeasonEnrollmentLog): SeasonEnrollmentLogEntity =
        SeasonEnrollmentLogEntity(
            id = domain.id,
            seasonEnrollmentId = domain.seasonEnrollmentId,
            enrollmentLogType = domain.enrollmentLogType,
            actorId = domain.actorId,
            note = domain.note,
            createdAt = domain.createdAt,
        )
}
