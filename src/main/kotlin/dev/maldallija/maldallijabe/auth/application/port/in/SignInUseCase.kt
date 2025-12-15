package dev.maldallija.maldallijabe.auth.application.port.`in`

interface SignInUseCase {
    fun signIn(
        username: String,
        password: String,
    ): SignInResult
}
