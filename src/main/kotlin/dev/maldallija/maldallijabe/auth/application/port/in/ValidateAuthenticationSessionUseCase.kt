package dev.maldallija.maldallijabe.auth.application.port.`in`

import java.util.UUID

interface ValidateAuthenticationSessionUseCase {
    fun validateAuthenticationSession(authenticationAccessSessionId: UUID): Long
}
