package dev.maldallija.maldallijabe.user.adapter.out.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "\"user\"")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(nullable = false, unique = true)
    val uuid: UUID,
    @Column(nullable = false, unique = true, length = 255)
    var username: String,
    @Column(nullable = false, length = 60)
    var password: String,
    @Column(nullable = false, length = 32)
    var nickname: String,
    @Column(nullable = false)
    var isSystemAdmin: Boolean,
    @Column(nullable = false, updatable = false)
    val createdAt: Instant,
    @Column(nullable = false)
    var updatedAt: Instant,
    var deletedAt: Instant?,
)
