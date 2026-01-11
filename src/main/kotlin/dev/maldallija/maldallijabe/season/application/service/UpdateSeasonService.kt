package dev.maldallija.maldallijabe.season.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.lesson.application.port.out.LessonRepository
import dev.maldallija.maldallijabe.season.application.port.`in`.UpdateSeasonUseCase
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import dev.maldallija.maldallijabe.season.domain.exception.CannotUpdateClosedSeasonException
import dev.maldallija.maldallijabe.season.domain.exception.InvalidDefaultTicketCountException
import dev.maldallija.maldallijabe.season.domain.exception.InvalidSeasonCapacityException
import dev.maldallija.maldallijabe.season.domain.exception.InvalidSeasonDateRangeException
import dev.maldallija.maldallijabe.season.domain.exception.InvalidSeasonTitleException
import dev.maldallija.maldallijabe.season.domain.exception.LessonsExistOutsideDateRangeException
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import dev.maldallija.maldallijabe.season.domain.exception.UnauthorizedSeasonOperationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional
class UpdateSeasonService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val seasonRepository: SeasonRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
    private val lessonRepository: LessonRepository,
) : UpdateSeasonUseCase {
    override fun updateSeason(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
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

        // 2. 시즌 조회
        val season =
            seasonRepository.findByUuid(seasonUuid)
                ?: throw SeasonNotFoundException()

        // 3. 시즌이 해당 승마장에 속하는지 검증
        if (season.equestrianCenterId != equestrianCenter.id) {
            throw SeasonNotFoundException()
        }

        // 4. 직원 권한 확인
        val staff =
            equestrianCenterStaffRepository.findActiveByEquestrianCenterIdAndUserId(
                equestrianCenterId = equestrianCenter.id,
                userId = requestingUserId,
            ) ?: throw UnauthorizedSeasonOperationException()

        // 5. 시즌 상태 확인 (CLOSED 시즌은 수정 불가)
        if (season.status == SeasonStatus.CLOSED) {
            throw CannotUpdateClosedSeasonException()
        }

        // 6. 입력값 검증
        if (title.isBlank()) {
            throw InvalidSeasonTitleException()
        }
        // NOTE: 과거 날짜로 시즌 수정 허용됨
        // - 시스템 도입 시 과거 시즌 데이터 입력 필요 (예: 작년 시즌 기록)
        // - 데이터 마이그레이션, 기록 관리 등의 use case
        // - 직원만 수정 가능하므로 의도적인 과거 데이터 수정은 valid
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

        // 7. 날짜 변경 시 기존 SCHEDULED 레슨이 새 범위 밖에 있는지 검증
        // NOTE: CANCELLED 레슨은 이미 취소되어 예약/환불 완료된 상태이므로 검증에서 제외
        val isDateChanged = season.startDate != startDate || season.endDate != endDate
        if (isDateChanged) {
            val scheduledLessonsExistOutsideRange =
                lessonRepository.existsBySeasonIdAndScheduledLessonDateOutsideRange(
                    seasonId = season.id,
                    startDate = startDate,
                    endDate = endDate,
                )
            if (scheduledLessonsExistOutsideRange) {
                throw LessonsExistOutsideDateRangeException()
            }
        }

        // 8. 시즌 수정
        // NOTE: defaultTicketCount 변경 시 기존 승인된 회원에게는 영향 없음
        // - 이미 부여된 티켓은 SeasonTicketAccount에 저장되어 있음
        // - 변경된 defaultTicketCount는 신규 승인 시에만 적용됨
        // - 기존 회원에게 추가 티켓이 필요한 경우 별도로 GrantAdditionalTicket 사용
        val updatedSeason =
            season.copy(
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                capacity = capacity,
                defaultTicketCount = defaultTicketCount,
                updatedBy = staff.id,
                updatedAt = Instant.now(),
            )

        seasonRepository.save(updatedSeason)
    }
}
