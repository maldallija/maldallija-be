package dev.maldallija.maldallijabe.equestriancenter.domain.exception

abstract class EquestrianCenterException(
    val errorCode: String,
    message: String,
) : RuntimeException(message)
