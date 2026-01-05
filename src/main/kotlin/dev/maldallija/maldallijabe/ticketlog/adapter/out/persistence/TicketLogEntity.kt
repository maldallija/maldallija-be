package dev.maldallija.maldallijabe.ticketlog.adapter.out.persistence

import dev.maldallija.maldallijabe.ticketlog.domain.TicketLogType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "ticket_log")
class TicketLogEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "season_ticket_account_id", nullable = false)
    val seasonTicketAccountId: Long,
    @Column(nullable = false)
    val amount: Int,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val ticketLogType: TicketLogType,
    @Column(length = 500)
    val description: String?,
    @Column(name = "reservation_id")
    val reservationId: Long?,
    @Column(name = "granted_by")
    val grantedBy: Long?,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
)
