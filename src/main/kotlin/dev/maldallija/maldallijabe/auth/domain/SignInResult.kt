package dev.maldallija.maldallijabe.auth.domain

data class SignInResult(
    val accessSession: AuthenticationAccessSession,
    val refreshSession: AuthenticationRefreshSession,
)
