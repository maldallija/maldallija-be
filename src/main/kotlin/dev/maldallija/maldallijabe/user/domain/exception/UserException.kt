package dev.maldallija.maldallijabe.user.domain.exception

import dev.maldallija.maldallijabe.common.domain.exception.BaseException

sealed class UserException(
    errorCode: String,
    message: String,
) : BaseException(
        errorCode = errorCode,
        message = message,
    )
