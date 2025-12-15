package dev.maldallija.maldallijabe.auth.application.service

import dev.maldallija.maldallijabe.auth.application.port.`in`.RefreshAuthenticationSessionUseCase
import dev.maldallija.maldallijabe.auth.application.port.`in`.SignInResult
import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationAccessSessionRepository
import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationRefreshSessionRepository
import dev.maldallija.maldallijabe.auth.config.AuthProperties
import dev.maldallija.maldallijabe.auth.domain.AuthenticationAccessSession
import dev.maldallija.maldallijabe.auth.domain.AuthenticationRefreshSession
import dev.maldallija.maldallijabe.auth.domain.AuthenticationSessionRevokedReason
import dev.maldallija.maldallijabe.auth.domain.exception.InvalidSessionException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class RefreshAuthenticationSessionService(
    private val authProperties: AuthProperties,
    private val authenticationAccessSessionRepository: AuthenticationAccessSessionRepository,
    private val authenticationRefreshSessionRepository: AuthenticationRefreshSessionRepository,
) : RefreshAuthenticationSessionUseCase {
    override fun refreshAuthenticationSession(authenticationRefreshSessionId: UUID): SignInResult {
        val authenticationRefreshSession =
            authenticationRefreshSessionRepository.findByAuthenticationRefreshSession(authenticationRefreshSessionId)
                ?: throw InvalidSessionException()

        if (!authenticationRefreshSession.isValid()) {
            throw InvalidSessionException()
        }

        val userId = authenticationRefreshSession.userId

        authenticationRefreshSessionRepository.revokeAllByUserId(
            userId,
            AuthenticationSessionRevokedReason.SESSION_REFRESH,
        )
        authenticationAccessSessionRepository.revokeAllByUserId(
            userId,
            AuthenticationSessionRevokedReason.SESSION_REFRESH,
        )

        val now = Instant.now()
        val newAuthenticationRefreshSession =
            AuthenticationRefreshSession(
                id = 0,
                authenticationRefreshSession = UUID.randomUUID(),
                userId = userId,
                createdAt = now,
                expiresAt = now.plusSeconds(authProperties.refreshSession.expiryDays * 24 * 60 * 60),
                revokedAt = null,
                revokedReason = null,
            )

        val newAuthenticationAccessSession =
            AuthenticationAccessSession(
                id = 0,
                authenticationAccessSession = UUID.randomUUID(),
                userId = userId,
                createdAt = now,
                expiresAt = now.plusSeconds(authProperties.accessSession.expiryHours * 60 * 60),
                revokedAt = null,
                revokedReason = null,
            )

        val savedAuthenticationRefreshSession =
            authenticationRefreshSessionRepository.save(newAuthenticationRefreshSession)
        val savedAuthenticationAccessSession =
            authenticationAccessSessionRepository.save(newAuthenticationAccessSession)

        return SignInResult(
            refreshSessionId = savedAuthenticationRefreshSession.authenticationRefreshSession,
            refreshSessionCreatedAt = savedAuthenticationRefreshSession.createdAt,
            refreshSessionExpiresAt = savedAuthenticationRefreshSession.expiresAt,
            accessSessionId = savedAuthenticationAccessSession.authenticationAccessSession,
            accessSessionCreatedAt = savedAuthenticationAccessSession.createdAt,
            accessSessionExpiresAt = savedAuthenticationAccessSession.expiresAt,
        )
    }
}
