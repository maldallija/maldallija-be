package dev.maldallija.maldallijabe.season.application.port.`in`

import java.util.UUID

interface CloseSeasonUseCase {
    fun closeSeason(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        requestingUserId: Long,
    )
}
