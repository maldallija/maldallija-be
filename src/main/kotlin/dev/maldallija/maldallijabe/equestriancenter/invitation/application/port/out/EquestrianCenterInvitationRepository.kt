package dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.out

import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.EquestrianCenterInvitation
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface EquestrianCenterInvitationRepository {
    fun existsByEquestrianCenterIdAndUserIdAndStatus(
        equestrianCenterId: Long,
        userId: Long,
        status: InvitationStatus,
    ): Boolean

    fun findByUuid(uuid: UUID): EquestrianCenterInvitation?

    fun findByEquestrianCenterIdAndOptionalStatus(
        equestrianCenterId: Long,
        status: InvitationStatus?,
        pageable: Pageable,
    ): Page<EquestrianCenterInvitation>

    fun findByUserIdAndOptionalStatus(
        userId: Long,
        status: InvitationStatus?,
        pageable: Pageable,
    ): Page<EquestrianCenterInvitation>

    fun save(equestrianCenterInvitation: EquestrianCenterInvitation): EquestrianCenterInvitation
}
