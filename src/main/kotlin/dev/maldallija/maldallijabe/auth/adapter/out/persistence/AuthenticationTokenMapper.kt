package dev.maldallija.maldallijabe.auth.adapter.out.persistence

import dev.maldallija.maldallijabe.auth.domain.AuthenticationToken
import org.springframework.stereotype.Component

@Component
class AuthenticationTokenMapper {
    fun toDomain(entity: AuthenticationTokenEntity): AuthenticationToken =
        AuthenticationToken(
            authenticationToken = entity.authenticationToken,
            userId = entity.userId,
            ipAddress = entity.ipAddress,
            userAgent = entity.userAgent,
            createdAt = entity.createdAt,
            expiresAt = entity.expiresAt,
            revokedAt = entity.revokedAt,
            revokedReason = entity.revokedReason,
        )

    fun toEntity(domain: AuthenticationToken): AuthenticationTokenEntity =
        AuthenticationTokenEntity(
            authenticationToken = domain.authenticationToken,
            userId = domain.userId,
            ipAddress = domain.ipAddress,
            userAgent = domain.userAgent,
            createdAt = domain.createdAt,
            expiresAt = domain.expiresAt,
            revokedAt = domain.revokedAt,
            revokedReason = domain.revokedReason,
        )
}
