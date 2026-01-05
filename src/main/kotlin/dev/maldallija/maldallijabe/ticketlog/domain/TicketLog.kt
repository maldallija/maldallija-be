package dev.maldallija.maldallijabe.ticketlog.domain

import java.time.Instant

data class TicketLog(
    val id: Long,
    val seasonTicketAccountId: Long,
    val amount: Int,
    val ticketLogType: TicketLogType,
    val description: String?,
    val reservationId: Long?,
    val grantedBy: Long?,
    val createdAt: Instant,
)
