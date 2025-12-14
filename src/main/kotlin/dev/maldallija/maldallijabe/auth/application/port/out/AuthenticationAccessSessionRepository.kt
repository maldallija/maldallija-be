package dev.maldallija.maldallijabe.auth.application.port.out

import dev.maldallija.maldallijabe.auth.domain.AuthenticationAccessSession
import java.util.UUID

interface AuthenticationAccessSessionRepository {
    fun save(accessSession: AuthenticationAccessSession): AuthenticationAccessSession

    fun findByAccessToken(accessToken: UUID): AuthenticationAccessSession?

    fun revokeAllByUserId(
        userId: Long,
        reason: String,
    )
}
