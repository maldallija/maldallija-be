package dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception

abstract class EquestrianCenterInvitationException(
    val errorCode: String,
    message: String,
) : RuntimeException(message)
