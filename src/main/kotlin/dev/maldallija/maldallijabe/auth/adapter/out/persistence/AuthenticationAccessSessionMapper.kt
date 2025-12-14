package dev.maldallija.maldallijabe.auth.adapter.out.persistence

import dev.maldallija.maldallijabe.auth.domain.AuthenticationAccessSession

object AuthenticationAccessSessionMapper {
    fun toDomain(entity: AuthenticationAccessSessionEntity): AuthenticationAccessSession =
        AuthenticationAccessSession(
            id = entity.id,
            authenticationAccessSession = entity.authenticationAccessSession,
            userId = entity.userId,
            createdAt = entity.createdAt,
            expiresAt = entity.expiresAt,
            revokedAt = entity.revokedAt,
            revokedReason = entity.revokedReason,
        )

    fun toEntity(domain: AuthenticationAccessSession): AuthenticationAccessSessionEntity =
        AuthenticationAccessSessionEntity(
            id = domain.id,
            authenticationAccessSession = domain.authenticationAccessSession,
            userId = domain.userId,
            createdAt = domain.createdAt,
            expiresAt = domain.expiresAt,
            revokedAt = domain.revokedAt,
            revokedReason = domain.revokedReason,
        )
}
