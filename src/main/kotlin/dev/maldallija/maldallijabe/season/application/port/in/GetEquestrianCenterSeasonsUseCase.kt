package dev.maldallija.maldallijabe.season.application.port.`in`

import dev.maldallija.maldallijabe.season.application.port.`in`.dto.SeasonSummary
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.UUID

interface GetEquestrianCenterSeasonsUseCase {
    fun getEquestrianCenterSeasons(
        equestrianCenterUuid: UUID,
        status: SeasonStatus?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        pageable: Pageable,
    ): Page<SeasonSummary>
}
