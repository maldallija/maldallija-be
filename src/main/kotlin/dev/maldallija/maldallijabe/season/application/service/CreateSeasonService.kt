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
import dev.maldallija.maldallijabe.season.domain.exception.InvalidSeasonTitleException
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

        // 3. 입력값 검증
        if (title.isBlank()) {
            throw InvalidSeasonTitleException()
        }
        // NOTE: 과거 날짜로 시즌 생성 허용됨
        // - 시스템 도입 시 과거 시즌 데이터 입력 필요 (예: 작년 시즌 기록)
        // - 데이터 마이그레이션, 기록 관리 등의 use case
        // - 직원만 생성 가능하므로 의도적인 과거 데이터 입력은 valid
        // - 프론트엔드에서 과거 날짜 입력 시 확인 메시지 권장 (실수 방지)
        // - 향후 startDate >= today 검증이 필요하면 추가 고려
        if (endDate.isBefore(startDate)) {
            throw InvalidSeasonDateRangeException()
        }
        if (capacity < 1) {
            throw InvalidSeasonCapacityException()
        }
        if (defaultTicketCount < 1) {
            throw InvalidDefaultTicketCountException()
        }

        // 4. 시즌 생성
        // NOTE: defaultTicketCount는 수강 신청 승인 시 부여할 기본 티켓 수
        // - 승인 시점의 season.defaultTicketCount 값을 읽어 SeasonTicketAccount에 부여
        // - 시즌 생성 후 defaultTicketCount를 변경해도 이미 승인된 회원에게는 영향 없음
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
                updatedBy = staff.id,
                updatedAt = now,
                deletedAt = null,
            )

        seasonRepository.save(season)
    }
}
