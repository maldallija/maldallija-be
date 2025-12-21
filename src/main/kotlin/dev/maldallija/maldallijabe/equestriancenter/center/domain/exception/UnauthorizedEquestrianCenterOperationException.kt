package dev.maldallija.maldallijabe.equestriancenter.center.domain.exception

class UnauthorizedEquestrianCenterOperationException :
    EquestrianCenterException(
        errorCode = "UNAUTHORIZED_EQUESTRIAN_CENTER_OPERATION",
        message = "Only system administrators can perform this operation",
    )
