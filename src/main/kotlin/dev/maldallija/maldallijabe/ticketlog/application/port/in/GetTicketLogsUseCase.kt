package dev.maldallija.maldallijabe.ticketlog.application.port.`in`

import dev.maldallija.maldallijabe.ticketlog.application.port.`in`.dto.TicketLogDetail
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface GetTicketLogsUseCase {
    fun getTicketLogs(
        userUuid: UUID,
        seasonUuid: UUID,
        requestingUserId: Long,
        pageable: Pageable,
    ): Page<TicketLogDetail>
}
