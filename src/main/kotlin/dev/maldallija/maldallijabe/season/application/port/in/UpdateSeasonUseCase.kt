package dev.maldallija.maldallijabe.season.application.port.`in`

import java.time.LocalDate
import java.util.UUID

interface UpdateSeasonUseCase {
    fun updateSeason(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        requestingUserId: Long,
        title: String,
        description: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        capacity: Int,
        defaultTicketCount: Int,
    )
}
