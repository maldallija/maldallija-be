package dev.maldallija.maldallijabe.user.domain.exception

class UnauthorizedUserOperationException :
    UserException(
        errorCode = "UNAUTHORIZED_USER_OPERATION",
        message = "Unauthorized user operation",
    )
