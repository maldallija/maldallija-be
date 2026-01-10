package dev.maldallija.maldallijabe.season.ticketaccount.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import dev.maldallija.maldallijabe.season.enrollment.application.port.out.SeasonEnrollmentRepository
import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
import dev.maldallija.maldallijabe.season.enrollment.domain.exception.InvalidEnrollmentStatusException
import dev.maldallija.maldallijabe.season.enrollment.domain.exception.SeasonEnrollmentNotFoundException
import dev.maldallija.maldallijabe.season.enrollment.domain.exception.UnauthorizedSeasonEnrollmentOperationException
import dev.maldallija.maldallijabe.season.ticketaccount.application.port.`in`.GrantAdditionalTicketsUseCase
import dev.maldallija.maldallijabe.season.ticketaccount.application.port.out.SeasonTicketAccountRepository
import dev.maldallija.maldallijabe.season.ticketaccount.domain.exception.CannotGrantTicketsToClosedSeasonException
import dev.maldallija.maldallijabe.season.ticketaccount.domain.exception.InvalidTicketAmountException
import dev.maldallija.maldallijabe.season.ticketaccount.domain.exception.TicketAccountNotFoundException
import dev.maldallija.maldallijabe.ticketlog.application.port.out.TicketLogRepository
import dev.maldallija.maldallijabe.ticketlog.domain.TicketLog
import dev.maldallija.maldallijabe.ticketlog.domain.TicketLogType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class GrantAdditionalTicketsService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val seasonRepository: SeasonRepository,
    private val seasonEnrollmentRepository: SeasonEnrollmentRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
    private val seasonTicketAccountRepository: SeasonTicketAccountRepository,
    private val ticketLogRepository: TicketLogRepository,
) : GrantAdditionalTicketsUseCase {
    override fun grantAdditionalTickets(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        enrollmentUuid: UUID,
        requestingUserId: Long,
        amount: Int,
        description: String?,
    ) {
        // 1. 티켓 수량 유효성 검증
        if (amount <= 0) {
            throw InvalidTicketAmountException()
        }

        // 2. 승마장 존재 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 3. 시즌 조회
        val season =
            seasonRepository.findByUuid(seasonUuid)
                ?: throw SeasonNotFoundException()

        // 4. 시즌이 해당 승마장에 속하는지 검증
        if (season.equestrianCenterId != equestrianCenter.id) {
            throw SeasonNotFoundException()
        }

        // 5. 시즌 상태 확인 (ACTIVE만 허용)
        if (season.status != SeasonStatus.ACTIVE) {
            throw CannotGrantTicketsToClosedSeasonException()
        }

        // 6. Staff 권한 확인
        val staff =
            equestrianCenterStaffRepository.findActiveByEquestrianCenterIdAndUserId(
                equestrianCenterId = equestrianCenter.id,
                userId = requestingUserId,
            ) ?: throw UnauthorizedSeasonEnrollmentOperationException()

        // 7. SeasonEnrollment 조회
        val enrollment =
            seasonEnrollmentRepository.findByUuid(enrollmentUuid)
                ?: throw SeasonEnrollmentNotFoundException()

        // 8. Enrollment이 해당 시즌에 속하는지 확인
        if (enrollment.seasonId != season.id) {
            throw SeasonEnrollmentNotFoundException()
        }

        // 9. 상태가 APPROVED인지 확인 (승인된 회원만 티켓 부여 가능)
        if (enrollment.enrollmentStatus != EnrollmentStatus.APPROVED) {
            throw InvalidEnrollmentStatusException()
        }

        // 10. SeasonTicketAccount 조회
        val ticketAccount =
            seasonTicketAccountRepository.findBySeasonIdAndMemberId(
                seasonId = season.id,
                memberId = enrollment.memberId,
            ) ?: throw TicketAccountNotFoundException()

        // 11. 잔액 업데이트 (불변 Entity → 새 인스턴스 생성 후 저장)
        val now = Instant.now()
        val updatedTicketAccount =
            ticketAccount.copy(
                balance = ticketAccount.balance + amount,
                updatedAt = now,
            )
        seasonTicketAccountRepository.save(updatedTicketAccount)

        // 12. TicketLog 생성 (ADDITIONAL)
        val ticketLog =
            TicketLog(
                id = 0L,
                seasonTicketAccountId = ticketAccount.id,
                amount = amount,
                ticketLogType = TicketLogType.ADDITIONAL,
                description = description,
                reservationId = null,
                grantedBy = staff.id,
                createdAt = now,
            )
        ticketLogRepository.save(ticketLog)
    }
}
