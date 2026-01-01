package dev.maldallija.maldallijabe.season.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.season.application.port.`in`.CreateSeasonUseCase
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.Season
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import dev.maldallija.maldallijabe.season.domain.exception.InvalidDefaultTicketCountException
import dev.maldallija.maldallijabe.season.domain.exception.InvalidSeasonCapacityException
import dev.maldallija.maldallijabe.season.domain.exception.InvalidSeasonDateRangeException
import dev.maldallija.maldallijabe.season.domain.exception.UnauthorizedSeasonOperationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
class CreateSeasonService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
    private val seasonRepository: SeasonRepository,
) : CreateSeasonUseCase {
    override fun createSeason(
        equestrianCenterUuid: UUID,
        requestingUserId: Long,
        title: String,
        description: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        capacity: Int,
        defaultTicketCount: Int,
    ) {
        // 1. 승마장 존재 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 2. 요청자가 해당 센터의 직원인지 확인
        val staff =
            equestrianCenterStaffRepository.findActiveByEquestrianCenterIdAndUserId(
                equestrianCenterId = equestrianCenter.id,
                userId = requestingUserId,
            ) ?: throw UnauthorizedSeasonOperationException()

        // 3. 날짜 범위 검증
        if (endDate.isBefore(startDate)) {
            throw InvalidSeasonDateRangeException()
        }

        // 4. 정원 검증
        if (capacity < 1) {
            throw InvalidSeasonCapacityException()
        }

        // 5. 기본 티켓 수 검증
        if (defaultTicketCount < 1) {
            throw InvalidDefaultTicketCountException()
        }

        // 6. 시즌 생성
        val now = Instant.now()
        val season =
            Season(
                id = 0L,
                uuid = UUID.randomUUID(),
                equestrianCenterId = equestrianCenter.id,
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                capacity = capacity,
                defaultTicketCount = defaultTicketCount,
                status = SeasonStatus.ACTIVE,
                createdBy = staff.id,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
            )

        seasonRepository.save(season)
    }
}
