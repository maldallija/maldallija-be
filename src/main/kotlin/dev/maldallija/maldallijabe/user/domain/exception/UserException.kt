package dev.maldallija.maldallijabe.user.domain.exception

abstract class UserException(
    val errorCode: String,
    message: String,
) : RuntimeException(message)
