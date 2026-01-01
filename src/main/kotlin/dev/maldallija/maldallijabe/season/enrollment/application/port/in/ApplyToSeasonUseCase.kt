package dev.maldallija.maldallijabe.season.enrollment.application.port.`in`

import java.util.UUID

interface ApplyToSeasonUseCase {
    fun applyToSeason(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        requestingUserId: Long,
    )
}
