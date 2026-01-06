package dev.maldallija.maldallijabe.user.domain.exception

class DuplicateUsernameException :
    UserException(
        errorCode = "DUPLICATE_USERNAME",
        message = "Username already exists",
    )
