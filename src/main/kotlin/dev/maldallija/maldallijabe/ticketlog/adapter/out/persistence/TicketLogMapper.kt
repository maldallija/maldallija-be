package dev.maldallija.maldallijabe.ticketlog.adapter.out.persistence

import dev.maldallija.maldallijabe.ticketlog.domain.TicketLog
import org.springframework.stereotype.Component

@Component
class TicketLogMapper {
    fun toDomain(entity: TicketLogEntity): TicketLog =
        TicketLog(
            id = entity.id,
            seasonTicketAccountId = entity.seasonTicketAccountId,
            amount = entity.amount,
            ticketLogType = entity.ticketLogType,
            description = entity.description,
            reservationId = entity.reservationId,
            grantedBy = entity.grantedBy,
            createdAt = entity.createdAt,
        )

    fun toEntity(domain: TicketLog): TicketLogEntity =
        TicketLogEntity(
            id = domain.id,
            seasonTicketAccountId = domain.seasonTicketAccountId,
            amount = domain.amount,
            ticketLogType = domain.ticketLogType,
            description = domain.description,
            reservationId = domain.reservationId,
            grantedBy = domain.grantedBy,
            createdAt = domain.createdAt,
        )
}
