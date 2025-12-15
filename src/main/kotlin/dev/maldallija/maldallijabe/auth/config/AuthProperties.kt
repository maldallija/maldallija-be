package dev.maldallija.maldallijabe.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth")
data class AuthProperties(
    val accessSession: AccessSession,
    val refreshSession: RefreshSession,
) {
    data class AccessSession(
        val expiryHours: Long,
    )

    data class RefreshSession(
        val expiryDays: Long,
    )
}
