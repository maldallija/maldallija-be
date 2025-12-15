package dev.maldallija.maldallijabe.auth.application.port.`in`

import java.util.UUID

interface RefreshSessionUseCase {
    fun refreshSession(refreshSessionId: UUID): SignInResult
}
