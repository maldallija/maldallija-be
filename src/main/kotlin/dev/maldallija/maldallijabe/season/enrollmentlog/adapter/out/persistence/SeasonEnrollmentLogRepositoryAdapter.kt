package dev.maldallija.maldallijabe.season.enrollmentlog.adapter.out.persistence

import dev.maldallija.maldallijabe.season.enrollmentlog.application.port.out.SeasonEnrollmentLogRepository
import dev.maldallija.maldallijabe.season.enrollmentlog.domain.SeasonEnrollmentLog
import org.springframework.stereotype.Component

@Component
class SeasonEnrollmentLogRepositoryAdapter(
    private val seasonEnrollmentLogJpaRepository: SeasonEnrollmentLogJpaRepository,
    private val seasonEnrollmentLogMapper: SeasonEnrollmentLogMapper,
) : SeasonEnrollmentLogRepository {
    override fun save(seasonEnrollmentLog: SeasonEnrollmentLog): SeasonEnrollmentLog {
        val entity = seasonEnrollmentLogMapper.toEntity(seasonEnrollmentLog)
        val savedEntity = seasonEnrollmentLogJpaRepository.save(entity)
        return seasonEnrollmentLogMapper.toDomain(savedEntity)
    }
}
