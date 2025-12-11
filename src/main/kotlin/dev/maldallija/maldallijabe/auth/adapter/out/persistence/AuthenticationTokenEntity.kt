package dev.maldallija.maldallijabe.auth.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "authentication_token")
class AuthenticationTokenEntity(
    @Id
    @Column(nullable = false, updatable = false)
    val authenticationToken: UUID,
    @Column(nullable = false)
    val userId: Long,
    @Column(length = 64)
    var ipAddress: String?,
    @Lob
    var userAgent: String?,
    @Column(nullable = false, updatable = false)
    val createdAt: Instant,
    @Column(nullable = false)
    var expiresAt: Instant,
    var revokedAt: Instant?,
    @Column(length = 64)
    var revokedReason: String?,
)
