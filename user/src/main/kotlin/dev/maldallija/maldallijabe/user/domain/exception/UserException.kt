package dev.maldallija.maldallijabe.user.domain.exception

sealed class UserException(
    val errorCode: String,
    message: String,
) : RuntimeException(message)
