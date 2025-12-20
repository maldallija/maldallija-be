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
@Table(name = "authentication_refresh_session")
class AuthenticationRefreshSessionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(nullable = false, unique = true)
    val authenticationRefreshSession: UUID,
    @Column(nullable = false)
    val userId: Long,
    @Column(nullable = false)
    val createdAt: Instant,
    @Column(nullable = false)
    val expiresAt: Instant,
    val revokedAt: Instant?,
    @Column(length = 64)
    val revokedReason: String?,
)
