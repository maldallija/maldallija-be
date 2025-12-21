package dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception

class InvitationExpiredException :
    EquestrianCenterInvitationException(
        errorCode = "INVITATION_EXPIRED",
        message = "Invitation has expired",
    )
