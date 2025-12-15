package dev.maldallija.maldallijabe.auth.application.port.`in`

import java.util.UUID

interface RefreshAuthenticationSessionUseCase {
    fun refreshAuthenticationSession(authenticationRefreshSessionId: UUID): SignInResult
}
