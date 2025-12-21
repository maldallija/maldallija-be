package dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`

import java.util.UUID

interface RejectEquestrianCenterInvitationUseCase {
    fun rejectEquestrianCenterInvitation(
        userUuid: UUID,
        invitationUuid: UUID,
        actorUserId: Long,
    )
}
