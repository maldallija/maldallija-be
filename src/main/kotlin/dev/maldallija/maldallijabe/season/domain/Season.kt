package dev.maldallija.maldallijabe.season.domain

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class Season(
    val id: Long,
    val uuid: UUID,
    val equestrianCenterId: Long,
    val title: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val capacity: Int,
    val defaultTicketCount: Int,
    val status: SeasonStatus,
    val createdBy: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
)
