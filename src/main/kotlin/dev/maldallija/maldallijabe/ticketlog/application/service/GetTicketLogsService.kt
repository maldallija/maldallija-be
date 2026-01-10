package dev.maldallija.maldallijabe.ticketlog.application.service

import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import dev.maldallija.maldallijabe.season.ticketaccount.application.port.out.SeasonTicketAccountRepository
import dev.maldallija.maldallijabe.season.ticketaccount.domain.exception.TicketAccountNotFoundException
import dev.maldallija.maldallijabe.ticketlog.application.port.`in`.GetTicketLogsUseCase
import dev.maldallija.maldallijabe.ticketlog.application.port.`in`.dto.TicketLogDetail
import dev.maldallija.maldallijabe.ticketlog.application.port.out.TicketLogRepository
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import dev.maldallija.maldallijabe.user.domain.exception.UnauthorizedUserOperationException
import dev.maldallija.maldallijabe.user.domain.exception.UserNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetTicketLogsService(
    private val userRepository: UserRepository,
    private val seasonRepository: SeasonRepository,
    private val seasonTicketAccountRepository: SeasonTicketAccountRepository,
    private val ticketLogRepository: TicketLogRepository,
) : GetTicketLogsUseCase {
    override fun getTicketLogs(
        userUuid: UUID,
        seasonUuid: UUID,
        requestingUserId: Long,
        pageable: Pageable,
    ): Page<TicketLogDetail> {
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

        // 5. TicketLog 목록 조회 및 DTO 변환
        return ticketLogRepository
            .findAllBySeasonTicketAccountId(
                seasonTicketAccountId = ticketAccount.id,
                pageable = pageable,
            ).map { ticketLog ->
                TicketLogDetail(
                    amount = ticketLog.amount,
                    ticketLogType = ticketLog.ticketLogType,
                    description = ticketLog.description,
                    createdAt = ticketLog.createdAt,
                )
            }
    }
}
