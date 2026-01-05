package dev.maldallija.maldallijabe.season.ticketaccount.application.port.out

import dev.maldallija.maldallijabe.season.ticketaccount.domain.SeasonTicketAccount

interface SeasonTicketAccountRepository {
    fun save(seasonTicketAccount: SeasonTicketAccount): SeasonTicketAccount

    fun findBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): SeasonTicketAccount?

    fun existsBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): Boolean
}
