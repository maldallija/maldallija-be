package dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception

class UnauthorizedInvitationOperationException :
    EquestrianCenterInvitationException(
        errorCode = "UNAUTHORIZED_INVITATION_OPERATION",
        message = "Only the invited user can approve or reject this invitation",
    )
