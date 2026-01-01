package dev.maldallija.maldallijabe.season.adapter.out.persistence

import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.Season
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

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

    override fun findByUuid(uuid: UUID): Season? =
        seasonJpaRepository
            .findByUuidAndDeletedAtIsNull(uuid)
            ?.let { seasonMapper.toDomain(it) }

    override fun findByEquestrianCenterIdAndSearchConditions(
        equestrianCenterId: Long,
        status: SeasonStatus?,
        searchStartDate: LocalDate?,
        searchEndDate: LocalDate?,
        pageable: Pageable,
    ): Page<Season> =
        when {
            // 상태 + 날짜 둘 다
            status != null && searchStartDate != null && searchEndDate != null -> {
                seasonJpaRepository.findByEquestrianCenterIdAndStatusAndDateRangeAndDeletedAtIsNull(
                    equestrianCenterId = equestrianCenterId,
                    status = status,
                    searchStartDate = searchStartDate,
                    searchEndDate = searchEndDate,
                    pageable = pageable,
                )
            }

            // 상태만
            status != null -> {
                seasonJpaRepository.findByEquestrianCenterIdAndStatusAndDeletedAtIsNull(
                    equestrianCenterId = equestrianCenterId,
                    status = status,
                    pageable = pageable,
                )
            }

            // 날짜만
            searchStartDate != null && searchEndDate != null -> {
                seasonJpaRepository.findByEquestrianCenterIdAndDateRangeAndDeletedAtIsNull(
                    equestrianCenterId = equestrianCenterId,
                    searchStartDate = searchStartDate,
                    searchEndDate = searchEndDate,
                    pageable = pageable,
                )
            }

            // 둘 다 없음 (전체)
            else -> {
                seasonJpaRepository.findByEquestrianCenterIdAndDeletedAtIsNull(
                    equestrianCenterId = equestrianCenterId,
                    pageable = pageable,
                )
            }
        }.map { seasonMapper.toDomain(it) }
}
