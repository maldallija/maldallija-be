package dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.out

import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.EquestrianCenterInvitation
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface EquestrianCenterInvitationRepository {
    fun existsByEquestrianCenterIdAndUserIdAndStatus(
        equestrianCenterId: Long,
        userId: Long,
        status: InvitationStatus,
    ): Boolean

    fun findByEquestrianCenterIdAndOptionalStatus(
        equestrianCenterId: Long,
        status: InvitationStatus?,
        pageable: Pageable,
    ): Page<EquestrianCenterInvitation>

    fun save(equestrianCenterInvitation: EquestrianCenterInvitation): EquestrianCenterInvitation
}
