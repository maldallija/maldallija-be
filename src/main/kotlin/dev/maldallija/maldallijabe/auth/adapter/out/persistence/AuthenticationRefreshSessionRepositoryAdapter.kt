package dev.maldallija.maldallijabe.auth.adapter.out.persistence

import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationRefreshSessionRepository
import dev.maldallija.maldallijabe.auth.domain.AuthenticationRefreshSession
import dev.maldallija.maldallijabe.auth.domain.AuthenticationSessionRevokedReason
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class AuthenticationRefreshSessionRepositoryAdapter(
    private val jpaRepository: AuthenticationRefreshSessionJpaRepository,
) : AuthenticationRefreshSessionRepository {
    override fun save(refreshSession: AuthenticationRefreshSession): AuthenticationRefreshSession {
        val entity = AuthenticationRefreshSessionMapper.toEntity(refreshSession)
        val savedEntity = jpaRepository.save(entity)
        return AuthenticationRefreshSessionMapper.toDomain(savedEntity)
    }

    override fun findByAuthenticationRefreshSession(authenticationRefreshSession: UUID): AuthenticationRefreshSession? =
        jpaRepository
            .findByAuthenticationRefreshSession(authenticationRefreshSession)
            ?.let { AuthenticationRefreshSessionMapper.toDomain(it) }

    override fun revokeAllByUserId(
        userId: Long,
        reason: AuthenticationSessionRevokedReason,
    ) {
        jpaRepository.revokeAllByUserId(
            userId = userId,
            reason = reason.value,
            revokedAt = Instant.now(),
        )
    }
}
