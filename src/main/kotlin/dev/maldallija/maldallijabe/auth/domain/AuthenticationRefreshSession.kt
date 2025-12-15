package dev.maldallija.maldallijabe.auth.domain

import java.time.Instant
import java.util.UUID

data class AuthenticationRefreshSession(
    val id: Long,
    val authenticationRefreshSession: UUID,
    val userId: Long,
    val createdAt: Instant,
    val expiresAt: Instant,
    val revokedAt: Instant?,
    val revokedReason: String?,
) {
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    fun isRevoked(): Boolean = revokedAt != null

    fun isValid(): Boolean = !isExpired() && !isRevoked()

    fun revoke(reason: String): AuthenticationRefreshSession =
        copy(
            revokedAt = Instant.now(),
            revokedReason = reason,
        )
}
