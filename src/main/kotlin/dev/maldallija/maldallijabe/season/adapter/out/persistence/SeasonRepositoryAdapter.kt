package dev.maldallija.maldallijabe.season.adapter.out.persistence

import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.Season
import org.springframework.stereotype.Repository

@Repository
class SeasonRepositoryAdapter(
    private val seasonJpaRepository: SeasonJpaRepository,
    private val seasonMapper: SeasonMapper,
) : SeasonRepository {
    override fun save(season: Season): Season {
        val entity = seasonMapper.toEntity(season)
        val savedEntity = seasonJpaRepository.save(entity)
        return seasonMapper.toDomain(savedEntity)
    }
}
