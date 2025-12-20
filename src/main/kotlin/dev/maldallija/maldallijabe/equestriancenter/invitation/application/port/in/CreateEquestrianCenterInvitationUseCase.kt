package dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`

import java.util.UUID

interface CreateEquestrianCenterInvitationUseCase {
    fun createEquestrianCenterInvitation(
        equestrianCenterUuid: UUID,
        requestingUserId: Long,
        invitedUserUuid: UUID,
    )
}
