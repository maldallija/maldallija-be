package dev.maldallija.maldallijabe.equestriancenter.invitation.adapter.out.persistence.repository

import dev.maldallija.maldallijabe.equestriancenter.invitation.adapter.out.persistence.mapper.EquestrianCenterInvitationMapper
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.out.EquestrianCenterInvitationRepository
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.EquestrianCenterInvitation
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import org.springframework.stereotype.Repository

@Repository
class EquestrianCenterInvitationRepositoryAdapter(
    private val equestrianCenterInvitationJpaRepository: EquestrianCenterInvitationJpaRepository,
    private val equestrianCenterInvitationMapper: EquestrianCenterInvitationMapper,
) : EquestrianCenterInvitationRepository {
    override fun existsByEquestrianCenterIdAndUserIdAndStatus(
        equestrianCenterId: Long,
        userId: Long,
        status: InvitationStatus,
    ): Boolean =
        equestrianCenterInvitationJpaRepository.existsByEquestrianCenterIdAndUserIdAndStatus(
            equestrianCenterId = equestrianCenterId,
            userId = userId,
            status = status,
        )

    override fun save(equestrianCenterInvitation: EquestrianCenterInvitation): EquestrianCenterInvitation {
        val entity = equestrianCenterInvitationMapper.toEntity(equestrianCenterInvitation)
        val savedEntity = equestrianCenterInvitationJpaRepository.save(entity)
        return equestrianCenterInvitationMapper.toDomain(savedEntity)
    }
}
