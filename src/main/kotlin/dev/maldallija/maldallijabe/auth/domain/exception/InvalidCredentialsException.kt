package dev.maldallija.maldallijabe.auth.domain.exception

class InvalidCredentialsException :
    AuthException(
        errorCode = "INVALID_CREDENTIALS",
        message = "Invalid username or password",
    )
