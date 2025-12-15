package dev.maldallija.maldallijabe.auth.domain

enum class AuthenticationSessionRevokedReason(
    val value: String,
) {
    NEW_SIGN_IN("NEW_SIGN_IN"),
    SIGN_OUT("SIGN_OUT"),
    SESSION_REFRESH("SESSION_REFRESH"),
}
