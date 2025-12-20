package dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception

class DuplicateInvitationException :
    EquestrianCenterInvitationException(
        errorCode = "DUPLICATE_INVITATION",
        message = "An invitation has already been sent to this user",
    )
