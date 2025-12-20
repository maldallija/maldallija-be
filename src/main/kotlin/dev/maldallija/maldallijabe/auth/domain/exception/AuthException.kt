package dev.maldallija.maldallijabe.auth.domain.exception

import dev.maldallija.maldallijabe.common.domain.exception.BaseException

sealed class AuthException(
    errorCode: String,
    message: String,
) : BaseException(
        errorCode = errorCode,
        message = message,
    )
