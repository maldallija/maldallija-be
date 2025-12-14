package dev.maldallija.maldallijabe.auth.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "authentication_access_session")
class AuthenticationAccessSessionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(nullable = false, unique = true)
    val accessToken: UUID,
    @Column(nullable = false)
    val userId: Long,
    @Column(nullable = false, updatable = false)
    val createdAt: Instant,
    @Column(nullable = false)
    var expiresAt: Instant,
    var revokedAt: Instant?,
    @Column(length = 64)
    var revokedReason: String?,
)
