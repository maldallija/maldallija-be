package dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence.mapper

import dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence.entity.EquestrianCenterStaffEntity
import dev.maldallija.maldallijabe.equestriancenter.staff.domain.EquestrianCenterStaff
import org.springframework.stereotype.Component

@Component
class EquestrianCenterStaffMapper {
    fun toDomain(entity: EquestrianCenterStaffEntity): EquestrianCenterStaff =
        EquestrianCenterStaff(
            id = entity.id,
            uuid = entity.uuid,
            equestrianCenterId = entity.equestrianCenterId,
            userId = entity.userId,
            joinedAt = entity.joinedAt,
            leftAt = entity.leftAt,
            leftBy = entity.leftBy,
            leftReason = entity.leftReason,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            updatedBy = entity.updatedBy,
        )

    fun toEntity(domain: EquestrianCenterStaff): EquestrianCenterStaffEntity =
        EquestrianCenterStaffEntity(
            id = domain.id,
            uuid = domain.uuid,
            equestrianCenterId = domain.equestrianCenterId,
            userId = domain.userId,
            joinedAt = domain.joinedAt,
            leftAt = domain.leftAt,
            leftBy = domain.leftBy,
            leftReason = domain.leftReason,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            updatedBy = domain.updatedBy,
            deletedAt = null,
        )
}
