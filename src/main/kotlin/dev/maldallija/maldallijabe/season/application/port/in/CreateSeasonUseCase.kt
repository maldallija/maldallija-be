package dev.maldallija.maldallijabe.season.application.port.`in`

import java.time.LocalDate
import java.util.UUID

interface CreateSeasonUseCase {
    fun createSeason(
        equestrianCenterUuid: UUID,
        requestingUserId: Long,
        title: String,
        description: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        capacity: Int,
        defaultTicketCount: Int,
    )
}
