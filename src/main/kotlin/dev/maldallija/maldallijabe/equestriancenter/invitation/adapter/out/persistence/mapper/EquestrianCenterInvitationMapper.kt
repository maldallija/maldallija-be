package dev.maldallija.maldallijabe.equestriancenter.invitation.adapter.out.persistence.mapper

import dev.maldallija.maldallijabe.equestriancenter.invitation.adapter.out.persistence.entity.EquestrianCenterInvitationEntity
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.EquestrianCenterInvitation
import org.springframework.stereotype.Component

@Component
class EquestrianCenterInvitationMapper {
    fun toDomain(entity: EquestrianCenterInvitationEntity): EquestrianCenterInvitation =
        EquestrianCenterInvitation(
            id = entity.id,
            uuid = entity.uuid,
            equestrianCenterId = entity.equestrianCenterId,
            userId = entity.userId,
            invitedBy = entity.invitedBy,
            status = entity.status,
            invitedAt = entity.invitedAt,
            respondedAt = entity.respondedAt,
            expiresAt = entity.expiresAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy,
        )

    fun toEntity(domain: EquestrianCenterInvitation): EquestrianCenterInvitationEntity =
        EquestrianCenterInvitationEntity(
            id = domain.id,
            uuid = domain.uuid,
            equestrianCenterId = domain.equestrianCenterId,
            userId = domain.userId,
            invitedBy = domain.invitedBy,
            status = domain.status,
            invitedAt = domain.invitedAt,
            respondedAt = domain.respondedAt,
            expiresAt = domain.expiresAt,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            updatedBy = domain.updatedBy,
        )
}
