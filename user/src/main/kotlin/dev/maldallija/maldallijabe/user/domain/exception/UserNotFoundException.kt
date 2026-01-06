package dev.maldallija.maldallijabe.user.domain.exception

class UserNotFoundException :
    UserException(
        errorCode = "USER_NOT_FOUND",
        message = "User not found",
    )
