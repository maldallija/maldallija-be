package dev.maldallija.maldallijabe.ticketlog.application.port.`in`.dto

import dev.maldallija.maldallijabe.ticketlog.domain.TicketLogType
import java.time.Instant

data class TicketLogDetail(
    val amount: Int,
    val ticketLogType: TicketLogType,
    val description: String?,
    val createdAt: Instant,
)
