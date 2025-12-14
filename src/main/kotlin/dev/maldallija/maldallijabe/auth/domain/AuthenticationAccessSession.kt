package dev.maldallija.maldallijabe.auth.domain

import java.time.Instant
import java.util.UUID

data class AuthenticationAccessSession(
    val id: Long,
    val authenticationAccessSession: UUID,
    val userId: Long,
    val createdAt: Instant,
    val expiresAt: Instant,
    val revokedAt: Instant?,
    val revokedReason: String?,
) {
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    fun isRevoked(): Boolean = revokedAt != null

    fun isValid(): Boolean = !isExpired() && !isRevoked()

    fun revoke(reason: String): AuthenticationAccessSession =
        copy(
            revokedAt = Instant.now(),
            revokedReason = reason,
        )
}
