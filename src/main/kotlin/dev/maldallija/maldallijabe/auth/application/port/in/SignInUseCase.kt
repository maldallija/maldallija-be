package dev.maldallija.maldallijabe.auth.application.port.`in`

import dev.maldallija.maldallijabe.auth.domain.AuthenticationAccessSession

interface SignInUseCase {
    fun signIn(
        username: String,
        password: String,
    ): AuthenticationAccessSession
}
