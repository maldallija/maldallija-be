package dev.maldallija.maldallijabe.season.application.port.`in`

import dev.maldallija.maldallijabe.season.application.port.`in`.dto.SeasonDetail
import java.util.UUID

interface GetSeasonDetailUseCase {
    fun getSeasonDetail(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
    ): SeasonDetail
}
