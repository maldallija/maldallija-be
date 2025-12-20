package dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.out

import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.EquestrianCenterInvitation
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus

interface EquestrianCenterInvitationRepository {
    fun existsByEquestrianCenterIdAndUserIdAndStatus(
        equestrianCenterId: Long,
        userId: Long,
        status: InvitationStatus,
    ): Boolean

    fun save(equestrianCenterInvitation: EquestrianCenterInvitation): EquestrianCenterInvitation
}
