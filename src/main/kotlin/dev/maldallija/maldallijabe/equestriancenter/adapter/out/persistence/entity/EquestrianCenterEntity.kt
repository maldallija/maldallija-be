package dev.maldallija.maldallijabe.equestriancenter.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "equestrian_center")
class EquestrianCenterEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(nullable = false, unique = true)
    val uuid: UUID,
    @Column(nullable = false, length = 128)
    val name: String,
    @Column(columnDefinition = "TEXT")
    val description: String?,
    @Column(nullable = false, name = "representative_user_id")
    val representativeUserId: Long,
    @Column(nullable = false)
    val createdBy: Long,
    @Column(nullable = false)
    val createdAt: Instant,
    @Column(nullable = false)
    val updatedBy: Long,
    @Column(nullable = false)
    val updatedAt: Instant,
    val deletedAt: Instant?,
)
