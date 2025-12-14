package dev.maldallija.maldallijabe.auth.adapter.out.persistence

import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationAccessSessionRepository
import dev.maldallija.maldallijabe.auth.domain.AuthenticationAccessSession
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class AuthenticationAccessSessionRepositoryAdapter(
    private val jpaRepository: AuthenticationAccessSessionJpaRepository,
) : AuthenticationAccessSessionRepository {
    override fun save(accessSession: AuthenticationAccessSession): AuthenticationAccessSession {
        val entity = AuthenticationAccessSessionMapper.toEntity(accessSession)
        val savedEntity = jpaRepository.save(entity)
        return AuthenticationAccessSessionMapper.toDomain(savedEntity)
    }

    override fun findByAuthenticationAccessSession(authenticationAccessSession: UUID): AuthenticationAccessSession? =
        jpaRepository
            .findByAuthenticationAccessSession(authenticationAccessSession)
            ?.let { AuthenticationAccessSessionMapper.toDomain(it) }

    override fun revokeAllByUserId(
        userId: Long,
        reason: String,
    ) {
        jpaRepository.revokeAllByUserId(
            userId = userId,
            reason = reason,
            revokedAt = Instant.now(),
        )
    }
}
