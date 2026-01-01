package dev.maldallija.maldallijabe.season.application.port.`in`.dto

import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import java.time.LocalDate
import java.util.UUID

data class SeasonSummary(
    val seasonUuid: UUID,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val capacity: Int,
    val status: SeasonStatus,
)
