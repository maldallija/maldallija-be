package dev.maldallija.maldallijabe.season.ticketaccount.application.service

import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import dev.maldallija.maldallijabe.season.ticketaccount.application.port.`in`.GetTicketAccountBalanceUseCase
import dev.maldallija.maldallijabe.season.ticketaccount.application.port.out.SeasonTicketAccountRepository
import dev.maldallija.maldallijabe.season.ticketaccount.domain.exception.TicketAccountNotFoundException
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import dev.maldallija.maldallijabe.user.domain.exception.UnauthorizedUserOperationException
import dev.maldallija.maldallijabe.user.domain.exception.UserNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetTicketAccountBalanceService(
    private val userRepository: UserRepository,
    private val seasonRepository: SeasonRepository,
    private val seasonTicketAccountRepository: SeasonTicketAccountRepository,
) : GetTicketAccountBalanceUseCase {
    override fun getTicketAccountBalance(
        userUuid: UUID,
        seasonUuid: UUID,
        requestingUserId: Long,
    ): Int {
        // 1. User 존재 확인
        val user =
            userRepository.findByUuid(userUuid)
                ?: throw UserNotFoundException()

        // 2. 본인인지 확인
        if (user.id != requestingUserId) {
            throw UnauthorizedUserOperationException()
        }

        // 3. Season 존재 확인
        val season =
            seasonRepository.findByUuid(seasonUuid)
                ?: throw SeasonNotFoundException()

        // 4. TicketAccount 조회
        val ticketAccount =
            seasonTicketAccountRepository.findBySeasonIdAndMemberId(
                seasonId = season.id,
                memberId = user.id,
            ) ?: throw TicketAccountNotFoundException()

        // 5. 잔액 반환
        return ticketAccount.balance
    }
}
