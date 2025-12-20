package dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception

class InvalidInvitationStatusException :
    EquestrianCenterInvitationException(
        errorCode = "INVALID_INVITATION_STATUS",
        message = "Only INVITED status can be withdrawn",
    )
