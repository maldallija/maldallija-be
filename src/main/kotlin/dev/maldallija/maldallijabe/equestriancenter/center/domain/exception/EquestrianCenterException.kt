package dev.maldallija.maldallijabe.equestriancenter.center.domain.exception

import dev.maldallija.maldallijabe.common.domain.exception.BaseException

sealed class EquestrianCenterException(
    errorCode: String,
    message: String,
) : BaseException(
        errorCode = errorCode,
        message = message,
    )
