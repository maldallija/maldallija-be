package dev.maldallija.maldallijabe.season.adapter.out.persistence

import dev.maldallija.maldallijabe.season.domain.Season
import org.springframework.stereotype.Component

@Component
class SeasonMapper {
    fun toDomain(entity: SeasonEntity): Season =
        Season(
            id = entity.id,
            uuid = entity.uuid,
            equestrianCenterId = entity.equestrianCenterId,
            title = entity.title,
            description = entity.description,
            startDate = entity.startDate,
            endDate = entity.endDate,
            capacity = entity.capacity,
            defaultTicketCount = entity.defaultTicketCount,
            status = entity.status,
            createdBy = entity.createdBy,
            createdAt = entity.createdAt,
            updatedBy = entity.updatedBy,
            updatedAt = entity.updatedAt,
            deletedAt = entity.deletedAt,
        )

    fun toEntity(domain: Season): SeasonEntity =
        SeasonEntity(
            id = domain.id,
            uuid = domain.uuid,
            equestrianCenterId = domain.equestrianCenterId,
            title = domain.title,
            description = domain.description,
            startDate = domain.startDate,
            endDate = domain.endDate,
            capacity = domain.capacity,
            defaultTicketCount = domain.defaultTicketCount,
            status = domain.status,
            createdBy = domain.createdBy,
            createdAt = domain.createdAt,
            updatedBy = domain.updatedBy,
            updatedAt = domain.updatedAt,
            deletedAt = domain.deletedAt,
        )
}
