package dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception

import dev.maldallija.maldallijabe.common.domain.exception.BaseException

sealed class EquestrianCenterInvitationException(
    errorCode: String,
    message: String,
) : BaseException(
        errorCode = errorCode,
        message = message,
    )
