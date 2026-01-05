package dev.maldallija.maldallijabe.ticketlog.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface TicketLogJpaRepository : JpaRepository<TicketLogEntity, Long>
