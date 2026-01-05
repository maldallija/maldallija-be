package dev.maldallija.maldallijabe.season.ticketaccount.adapter.out.persistence

import dev.maldallija.maldallijabe.season.ticketaccount.application.port.out.SeasonTicketAccountRepository
import dev.maldallija.maldallijabe.season.ticketaccount.domain.SeasonTicketAccount
import org.springframework.stereotype.Repository

@Repository
class SeasonTicketAccountRepositoryAdapter(
    private val seasonTicketAccountJpaRepository: SeasonTicketAccountJpaRepository,
    private val seasonTicketAccountMapper: SeasonTicketAccountMapper,
) : SeasonTicketAccountRepository {
    override fun save(seasonTicketAccount: SeasonTicketAccount): SeasonTicketAccount {
        val entity = seasonTicketAccountMapper.toEntity(seasonTicketAccount)
        val savedEntity = seasonTicketAccountJpaRepository.save(entity)
        return seasonTicketAccountMapper.toDomain(savedEntity)
    }

    override fun findBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): SeasonTicketAccount? =
        seasonTicketAccountJpaRepository
            .findBySeasonIdAndMemberId(
                seasonId = seasonId,
                memberId = memberId,
            )?.let { seasonTicketAccountMapper.toDomain(it) }

    override fun existsBySeasonIdAndMemberId(
        seasonId: Long,
        memberId: Long,
    ): Boolean =
        seasonTicketAccountJpaRepository.existsBySeasonIdAndMemberId(
            seasonId = seasonId,
            memberId = memberId,
        )
}
