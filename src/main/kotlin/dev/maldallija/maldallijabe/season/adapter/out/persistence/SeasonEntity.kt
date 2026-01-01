package dev.maldallija.maldallijabe.season.adapter.out.persistence

import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "season")
class SeasonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(nullable = false, unique = true)
    val uuid: UUID,
    @Column(name = "equestrian_center_id", nullable = false)
    val equestrianCenterId: Long,
    @Column(nullable = false, length = 200)
    val title: String,
    @Column(columnDefinition = "TEXT")
    val description: String?,
    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,
    @Column(name = "end_date", nullable = false)
    val endDate: LocalDate,
    @Column(nullable = false)
    val capacity: Int,
    @Column(name = "default_ticket_count", nullable = false)
    val defaultTicketCount: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: SeasonStatus,
    @Column(name = "created_by", nullable = false)
    val createdBy: Long,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "updated_by", nullable = false)
    val updatedBy: Long,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
    @Column(name = "deleted_at")
    val deletedAt: Instant?,
)
