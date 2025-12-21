package dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception

class InvitationAlreadyRespondedException :
    EquestrianCenterInvitationException(
        errorCode = "INVITATION_ALREADY_RESPONDED",
        message = "Invitation has already been approved, rejected, or withdrawn",
    )
