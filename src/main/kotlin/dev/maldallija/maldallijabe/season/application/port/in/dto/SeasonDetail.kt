package dev.maldallija.maldallijabe.season.application.port.`in`.dto

import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class SeasonDetail(
    val seasonUuid: UUID,
    val equestrianCenterUuid: UUID,
    val equestrianCenterName: String,
    val title: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val capacity: Int,
    val defaultTicketCount: Int,
    val status: SeasonStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)
