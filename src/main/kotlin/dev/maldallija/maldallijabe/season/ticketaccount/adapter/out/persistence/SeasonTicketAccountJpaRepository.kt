package dev.maldallija.maldallijabe.season.ticketaccount.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface SeasonTicketAccountJpaRepository : JpaRepository<SeasonTicketAccountEntity, Long> {
    fun findBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): SeasonTicketAccountEntity?

    fun existsBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): Boolean
}
