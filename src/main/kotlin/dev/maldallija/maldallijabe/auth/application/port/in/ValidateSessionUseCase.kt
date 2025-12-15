package dev.maldallija.maldallijabe.auth.application.port.`in`

import java.util.UUID

interface ValidateSessionUseCase {
    fun validateSession(sessionId: UUID): Long
}
