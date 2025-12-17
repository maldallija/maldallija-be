package dev.maldallija.maldallijabe.equestriancenter.domain.exception

class EquestrianCenterNotFoundException :
    EquestrianCenterException(
        errorCode = "EQUESTRIAN_CENTER_NOT_FOUND",
        message = "Equestrian center not found",
    )
