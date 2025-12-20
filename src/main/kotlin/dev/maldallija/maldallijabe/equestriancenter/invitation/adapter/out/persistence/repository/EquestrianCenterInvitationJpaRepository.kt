package dev.maldallija.maldallijabe.equestriancenter.invitation.adapter.out.persistence.repository

import dev.maldallija.maldallijabe.equestriancenter.invitation.adapter.out.persistence.entity.EquestrianCenterInvitationEntity
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import org.springframework.data.jpa.repository.JpaRepository

interface EquestrianCenterInvitationJpaRepository : JpaRepository<EquestrianCenterInvitationEntity, Long> {
    fun existsByEquestrianCenterIdAndUserIdAndStatus(
        equestrianCenterId: Long,
        userId: Long,
        status: InvitationStatus,
    ): Boolean
}
