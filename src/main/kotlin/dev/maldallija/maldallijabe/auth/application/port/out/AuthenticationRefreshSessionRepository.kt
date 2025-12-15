package dev.maldallija.maldallijabe.auth.application.port.out

import dev.maldallija.maldallijabe.auth.domain.AuthenticationRefreshSession
import java.util.UUID

interface AuthenticationRefreshSessionRepository {
    fun save(refreshSession: AuthenticationRefreshSession): AuthenticationRefreshSession

    fun findByAuthenticationRefreshSession(authenticationRefreshSession: UUID): AuthenticationRefreshSession?

    fun revokeAllByUserId(
        userId: Long,
        reason: String,
    )
}
