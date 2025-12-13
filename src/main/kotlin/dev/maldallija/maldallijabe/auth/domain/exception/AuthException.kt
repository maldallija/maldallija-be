package dev.maldallija.maldallijabe.auth.domain.exception

abstract class AuthException(
    val errorCode: String,
    message: String,
) : RuntimeException(message)
