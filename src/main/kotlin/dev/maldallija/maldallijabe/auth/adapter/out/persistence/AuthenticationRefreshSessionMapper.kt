package dev.maldallija.maldallijabe.auth.adapter.out.persistence

import dev.maldallija.maldallijabe.auth.domain.AuthenticationRefreshSession

object AuthenticationRefreshSessionMapper {
    fun toDomain(entity: AuthenticationRefreshSessionEntity): AuthenticationRefreshSession =
        AuthenticationRefreshSession(
            id = entity.id,
            authenticationRefreshSession = entity.authenticationRefreshSession,
            userId = entity.userId,
            createdAt = entity.createdAt,
            expiresAt = entity.expiresAt,
            revokedAt = entity.revokedAt,
            revokedReason = entity.revokedReason,
        )

    fun toEntity(domain: AuthenticationRefreshSession): AuthenticationRefreshSessionEntity =
        AuthenticationRefreshSessionEntity(
            id = domain.id,
            authenticationRefreshSession = domain.authenticationRefreshSession,
            userId = domain.userId,
            createdAt = domain.createdAt,
            expiresAt = domain.expiresAt,
            revokedAt = domain.revokedAt,
            revokedReason = domain.revokedReason,
        )
}
