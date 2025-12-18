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
    var name: String,
    @Column(columnDefinition = "TEXT")
    var description: String?,
    @Column(nullable = false, name = "representative_user_id")
    var representativeUserId: Long,
    @Column(nullable = false, updatable = false)
    val createdBy: Long,
    @Column(nullable = false, updatable = false)
    val createdAt: Instant,
    @Column(nullable = false)
    var updatedBy: Long,
    @Column(nullable = false)
    var updatedAt: Instant,
    var deletedAt: Instant?,
)
