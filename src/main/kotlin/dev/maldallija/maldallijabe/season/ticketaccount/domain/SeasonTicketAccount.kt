package dev.maldallija.maldallijabe.season.ticketaccount.domain

import java.time.Instant

data class SeasonTicketAccount(
    val id: Long,
    val seasonId: Long,
    val memberId: Long,
    val balance: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
