package dev.maldallija.maldallijabe.ticketlog.adapter.out.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface TicketLogJpaRepository : JpaRepository<TicketLogEntity, Long> {
    fun findAllBySeasonTicketAccountId(
        seasonTicketAccountId: Long,
        pageable: Pageable,
    ): Page<TicketLogEntity>
}
