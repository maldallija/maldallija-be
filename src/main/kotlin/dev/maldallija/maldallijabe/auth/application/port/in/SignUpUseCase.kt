package dev.maldallija.maldallijabe.auth.application.port.`in`

import dev.maldallija.maldallijabe.user.domain.User

interface SignUpUseCase {
    fun signUp(
        username: String,
        password: String,
        nickname: String,
    ): User
}
