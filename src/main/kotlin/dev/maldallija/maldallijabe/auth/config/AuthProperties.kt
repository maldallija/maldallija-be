package dev.maldallija.maldallijabe.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auth")
data class AuthProperties(
    val accessSession: AccessSession,
) {
    data class AccessSession(
        val expiryDays: Long,
    )
}
