package dev.maldallija.maldallijabe.season.ticketaccount.adapter.out.persistence

import dev.maldallija.maldallijabe.season.ticketaccount.domain.SeasonTicketAccount
import org.springframework.stereotype.Component

@Component
class SeasonTicketAccountMapper {
    fun toDomain(entity: SeasonTicketAccountEntity): SeasonTicketAccount =
        SeasonTicketAccount(
            id = entity.id,
            seasonId = entity.seasonId,
            memberId = entity.memberId,
            balance = entity.balance,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(domain: SeasonTicketAccount): SeasonTicketAccountEntity =
        SeasonTicketAccountEntity(
            id = domain.id,
            seasonId = domain.seasonId,
            memberId = domain.memberId,
            balance = domain.balance,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
        )
}
