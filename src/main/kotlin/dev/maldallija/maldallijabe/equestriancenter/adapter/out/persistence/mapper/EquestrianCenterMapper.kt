package dev.maldallija.maldallijabe.equestriancenter.adapter.out.persistence.mapper

import dev.maldallija.maldallijabe.equestriancenter.adapter.out.persistence.entity.EquestrianCenterEntity
import dev.maldallija.maldallijabe.equestriancenter.domain.EquestrianCenter
import org.springframework.stereotype.Component

@Component
class EquestrianCenterMapper {
    fun toDomain(entity: EquestrianCenterEntity): EquestrianCenter =
        EquestrianCenter(
            id = entity.id,
            uuid = entity.uuid,
            name = entity.name,
            description = entity.description,
            representativeUserId = entity.representativeUserId,
            createdBy = entity.createdBy,
            createdAt = entity.createdAt,
            updatedBy = entity.updatedBy,
            updatedAt = entity.updatedAt,
            deletedAt = entity.deletedAt,
        )

    fun toEntity(domain: EquestrianCenter): EquestrianCenterEntity =
        EquestrianCenterEntity(
            id = domain.id,
            uuid = domain.uuid,
            name = domain.name,
            description = domain.description,
            representativeUserId = domain.representativeUserId,
            createdBy = domain.createdBy,
            createdAt = domain.createdAt,
            updatedBy = domain.updatedBy,
            updatedAt = domain.updatedAt,
            deletedAt = domain.deletedAt,
        )
}
