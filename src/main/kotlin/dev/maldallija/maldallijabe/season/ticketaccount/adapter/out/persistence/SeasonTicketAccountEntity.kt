package dev.maldallija.maldallijabe.season.ticketaccount.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "season_ticket_account")
class SeasonTicketAccountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "season_id", nullable = false)
    val seasonId: Long,
    @Column(name = "member_id", nullable = false)
    val memberId: Long,
    @Column(nullable = false)
    val balance: Int,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
)
