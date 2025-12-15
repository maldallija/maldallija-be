package dev.maldallija.maldallijabe.auth.application.port.out

import dev.maldallija.maldallijabe.auth.domain.AuthenticationAccessSession
import dev.maldallija.maldallijabe.auth.domain.AuthenticationSessionRevokedReason
import java.util.UUID

interface AuthenticationAccessSessionRepository {
    fun save(accessSession: AuthenticationAccessSession): AuthenticationAccessSession

    fun findByAuthenticationAccessSession(authenticationAccessSession: UUID): AuthenticationAccessSession?

    fun revokeAllByUserId(
        userId: Long,
        reason: AuthenticationSessionRevokedReason,
    )
}
