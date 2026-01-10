package dev.maldallija.maldallijabe.ticketlog.adapter.`in`.web.dto

import dev.maldallija.maldallijabe.ticketlog.domain.TicketLogType
import java.time.Instant

data class TicketLogResponse(
    val amount: Int,
    val ticketLogType: TicketLogType,
    val description: String?,
    val createdAt: Instant,
)
