package dev.maldallija.maldallijabe.season.application.port.out

import dev.maldallija.maldallijabe.season.domain.Season
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.UUID

interface SeasonRepository {
    fun save(season: Season): Season

    fun findByUuid(uuid: UUID): Season?

    fun findByEquestrianCenterIdAndSearchConditions(
        equestrianCenterId: Long,
        status: SeasonStatus?,
        searchStartDate: LocalDate?,
        searchEndDate: LocalDate?,
        pageable: Pageable,
    ): Page<Season>
}
