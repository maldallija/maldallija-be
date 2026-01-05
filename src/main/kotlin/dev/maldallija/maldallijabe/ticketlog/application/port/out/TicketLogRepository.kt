package dev.maldallija.maldallijabe.ticketlog.application.port.out

import dev.maldallija.maldallijabe.ticketlog.domain.TicketLog

interface TicketLogRepository {
    fun save(ticketLog: TicketLog): TicketLog
}
