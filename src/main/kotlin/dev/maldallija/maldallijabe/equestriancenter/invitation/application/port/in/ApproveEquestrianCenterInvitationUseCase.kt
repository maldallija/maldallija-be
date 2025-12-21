package dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`

import java.util.UUID

interface ApproveEquestrianCenterInvitationUseCase {
    fun approveEquestrianCenterInvitation(
        userUuid: UUID,
        invitationUuid: UUID,
        actorUserId: Long,
    )
}
