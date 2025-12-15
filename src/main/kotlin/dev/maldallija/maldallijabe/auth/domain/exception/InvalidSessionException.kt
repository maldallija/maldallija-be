package dev.maldallija.maldallijabe.auth.domain.exception

class InvalidSessionException :
    AuthException(
        errorCode = "INVALID_SESSION",
        message = "Invalid or expired session",
    )
