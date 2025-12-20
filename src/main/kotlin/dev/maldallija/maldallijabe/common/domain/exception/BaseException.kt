package dev.maldallija.maldallijabe.common.domain.exception

abstract class BaseException(
    val errorCode: String,
    message: String,
) : RuntimeException(message)
