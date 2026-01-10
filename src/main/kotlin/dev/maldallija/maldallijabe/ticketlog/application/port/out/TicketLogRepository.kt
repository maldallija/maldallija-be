package dev.maldallija.maldallijabe.ticketlog.application.port.out

import dev.maldallija.maldallijabe.ticketlog.domain.TicketLog
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface TicketLogRepository {
    fun save(ticketLog: TicketLog): TicketLog

    fun findAllBySeasonTicketAccountId(
        seasonTicketAccountId: Long,
        pageable: Pageable,
    ): Page<TicketLog>
}
