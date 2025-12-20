package dev.maldallija.maldallijabe.equestriancenter.invitation.adapter.out.persistence.repository

import dev.maldallija.maldallijabe.equestriancenter.invitation.adapter.out.persistence.entity.EquestrianCenterInvitationEntity
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface EquestrianCenterInvitationJpaRepository : JpaRepository<EquestrianCenterInvitationEntity, Long> {
    fun existsByEquestrianCenterIdAndUserIdAndStatus(
        equestrianCenterId: Long,
        userId: Long,
        status: InvitationStatus,
    ): Boolean

    @Query(
        """
        SELECT e FROM EquestrianCenterInvitationEntity e
        WHERE e.equestrianCenterId = :equestrianCenterId
        AND (:status IS NULL OR e.status = :status)
        """,
    )
    fun findByEquestrianCenterIdAndOptionalStatus(
        equestrianCenterId: Long,
        status: InvitationStatus?,
        pageable: Pageable,
    ): Page<EquestrianCenterInvitationEntity>
}
