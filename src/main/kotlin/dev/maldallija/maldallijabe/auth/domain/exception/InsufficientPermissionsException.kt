package dev.maldallija.maldallijabe.auth.domain.exception

class InsufficientPermissionsException :
    AuthException(
        errorCode = "INSUFFICIENT_PERMISSIONS",
        message = "Insufficient permissions to access this resource",
    )
