package dev.maldallija.maldallijabe.auth.adapter.out.persistence

import dev.maldallija.maldallijabe.auth.domain.AuthenticationAccessSession

object AuthenticationAccessSessionMapper {
    fun toDomain(entity: AuthenticationAccessSessionEntity): AuthenticationAccessSession =
        AuthenticationAccessSession(
            id = entity.id,
            accessToken = entity.accessToken,
            userId = entity.userId,
            createdAt = entity.createdAt,
            expiresAt = entity.expiresAt,
            revokedAt = entity.revokedAt,
            revokedReason = entity.revokedReason,
        )

    fun toEntity(domain: AuthenticationAccessSession): AuthenticationAccessSessionEntity =
        AuthenticationAccessSessionEntity(
            id = domain.id,
            accessToken = domain.accessToken,
            userId = domain.userId,
            createdAt = domain.createdAt,
            expiresAt = domain.expiresAt,
            revokedAt = domain.revokedAt,
            revokedReason = domain.revokedReason,
        )
}
