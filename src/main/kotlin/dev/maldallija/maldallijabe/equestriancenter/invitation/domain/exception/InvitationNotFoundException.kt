package dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception

class InvitationNotFoundException :
    EquestrianCenterInvitationException(
        errorCode = "INVITATION_NOT_FOUND",
        message = "Invitation not found",
    )
