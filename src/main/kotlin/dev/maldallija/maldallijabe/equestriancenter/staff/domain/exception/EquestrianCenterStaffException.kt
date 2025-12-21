package dev.maldallija.maldallijabe.equestriancenter.staff.domain.exception

import dev.maldallija.maldallijabe.common.domain.exception.BaseException

sealed class EquestrianCenterStaffException(
    errorCode: String,
    message: String,
) : BaseException(errorCode, message)
