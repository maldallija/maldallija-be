package dev.maldallija.maldallijabe.auth.application.port.`in`

import dev.maldallija.maldallijabe.auth.domain.SignInResult

interface SignInUseCase {
    fun signIn(
        username: String,
        password: String,
    ): SignInResult
}
