package dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`

import java.util.UUID

interface WithdrawEquestrianCenterInvitationUseCase {
    fun withdrawEquestrianCenterInvitation(
        equestrianCenterUuid: UUID,
        invitationUuid: UUID,
        requestingUserId: Long,
    )
}
