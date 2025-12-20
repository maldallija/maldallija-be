package dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception

class AlreadyStaffMemberException :
    EquestrianCenterInvitationException(
        errorCode = "ALREADY_STAFF_MEMBER",
        message = "User is already a staff member of this equestrian center",
    )
