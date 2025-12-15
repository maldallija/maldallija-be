package dev.maldallija.maldallijabe.auth.application.service

import dev.maldallija.maldallijabe.auth.application.port.`in`.RefreshSessionUseCase
import dev.maldallija.maldallijabe.auth.application.port.`in`.SignInResult
import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationAccessSessionRepository
import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationRefreshSessionRepository
import dev.maldallija.maldallijabe.auth.config.AuthProperties
import dev.maldallija.maldallijabe.auth.domain.AuthenticationAccessSession
import dev.maldallija.maldallijabe.auth.domain.AuthenticationRefreshSession
import dev.maldallija.maldallijabe.auth.domain.exception.InvalidSessionException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class RefreshSessionService(
    private val authProperties: AuthProperties,
    private val authenticationAccessSessionRepository: AuthenticationAccessSessionRepository,
    private val authenticationRefreshSessionRepository: AuthenticationRefreshSessionRepository,
) : RefreshSessionUseCase {
    override fun refreshSession(refreshSessionId: UUID): SignInResult {
        val refreshSession =
            authenticationRefreshSessionRepository.findByAuthenticationRefreshSession(refreshSessionId)
                ?: throw InvalidSessionException()

        if (!refreshSession.isValid()) {
            throw InvalidSessionException()
        }

        val userId = refreshSession.userId

        authenticationRefreshSessionRepository.revokeAllByUserId(userId, "SESSION_REFRESH")
        authenticationAccessSessionRepository.revokeAllByUserId(userId, "SESSION_REFRESH")

        val now = Instant.now()
        val authenticationRefreshSession =
            AuthenticationRefreshSession(
                id = 0,
                authenticationRefreshSession = UUID.randomUUID(),
                userId = userId,
                createdAt = now,
                expiresAt = now.plusSeconds(authProperties.refreshSession.expiryDays * 24 * 60 * 60),
                revokedAt = null,
                revokedReason = null,
            )

        val authenticationAccessSession =
            AuthenticationAccessSession(
                id = 0,
                authenticationAccessSession = UUID.randomUUID(),
                userId = userId,
                createdAt = now,
                expiresAt = now.plusSeconds(authProperties.accessSession.expiryHours * 60 * 60),
                revokedAt = null,
                revokedReason = null,
            )

        val savedRefreshSession = authenticationRefreshSessionRepository.save(authenticationRefreshSession)
        val savedAccessSession = authenticationAccessSessionRepository.save(authenticationAccessSession)

        return SignInResult(
            refreshSessionId = savedRefreshSession.authenticationRefreshSession,
            refreshSessionCreatedAt = savedRefreshSession.createdAt,
            refreshSessionExpiresAt = savedRefreshSession.expiresAt,
            accessSessionId = savedAccessSession.authenticationAccessSession,
            accessSessionCreatedAt = savedAccessSession.createdAt,
            accessSessionExpiresAt = savedAccessSession.expiresAt,
        )
    }
}
