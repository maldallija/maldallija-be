package dev.maldallija.maldallijabe.auth.application.port.`in`

import java.time.Instant
import java.util.UUID

data class SignInResult(
    val refreshSessionId: UUID,
    val refreshSessionCreatedAt: Instant,
    val refreshSessionExpiresAt: Instant,
    val accessSessionId: UUID,
    val accessSessionCreatedAt: Instant,
    val accessSessionExpiresAt: Instant,
)
