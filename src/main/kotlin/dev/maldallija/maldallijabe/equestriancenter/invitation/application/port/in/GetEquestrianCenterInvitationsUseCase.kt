package dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`

import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.dto.EquestrianCenterInvitationDetail
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface GetEquestrianCenterInvitationsUseCase {
    fun getEquestrianCenterInvitations(
        equestrianCenterUuid: UUID,
        requestingUserId: Long,
        status: InvitationStatus?,
        pageable: Pageable,
    ): Page<EquestrianCenterInvitationDetail>
}
