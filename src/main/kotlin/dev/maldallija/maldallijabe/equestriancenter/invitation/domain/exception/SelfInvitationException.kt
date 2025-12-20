package dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception

class SelfInvitationException :
    EquestrianCenterInvitationException(
        errorCode = "SELF_INVITATION",
        message = "Cannot invite yourself",
    )
