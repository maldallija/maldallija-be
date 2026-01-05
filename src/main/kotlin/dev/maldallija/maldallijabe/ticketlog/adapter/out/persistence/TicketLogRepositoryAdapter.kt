package dev.maldallija.maldallijabe.ticketlog.adapter.out.persistence

import dev.maldallija.maldallijabe.ticketlog.application.port.out.TicketLogRepository
import dev.maldallija.maldallijabe.ticketlog.domain.TicketLog
import org.springframework.stereotype.Repository

@Repository
class TicketLogRepositoryAdapter(
    private val ticketLogJpaRepository: TicketLogJpaRepository,
    private val ticketLogMapper: TicketLogMapper,
) : TicketLogRepository {
    override fun save(ticketLog: TicketLog): TicketLog {
        val entity = ticketLogMapper.toEntity(ticketLog)
        val savedEntity = ticketLogJpaRepository.save(entity)
        return ticketLogMapper.toDomain(savedEntity)
    }
}
