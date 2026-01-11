package dev.maldallija.maldallijabe.lesson.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.lesson.application.port.`in`.CancelLessonUseCase
import dev.maldallija.maldallijabe.lesson.application.port.out.LessonRepository
import dev.maldallija.maldallijabe.lesson.domain.LessonStatus
import dev.maldallija.maldallijabe.lesson.domain.exception.LessonNotFoundException
import dev.maldallija.maldallijabe.lesson.domain.exception.LessonNotScheduledException
import dev.maldallija.maldallijabe.lesson.domain.exception.SeasonNotActiveException
import dev.maldallija.maldallijabe.lesson.domain.exception.UnauthorizedLessonOperationException
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class CancelLessonService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
    private val seasonRepository: SeasonRepository,
    private val lessonRepository: LessonRepository,
) : CancelLessonUseCase {
    override fun cancelLesson(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        lessonUuid: UUID,
        requestingUserId: Long,
    ) {
        // 1. 승마장 존재 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 2. 요청자가 해당 센터의 직원인지 확인
        equestrianCenterStaffRepository.findActiveByEquestrianCenterIdAndUserId(
            equestrianCenterId = equestrianCenter.id,
            userId = requestingUserId,
        ) ?: throw UnauthorizedLessonOperationException()

        // 3. 시즌 존재 및 해당 센터 소속 확인
        val season =
            seasonRepository.findByUuid(seasonUuid)
                ?: throw SeasonNotFoundException()

        if (season.equestrianCenterId != equestrianCenter.id) {
            throw SeasonNotFoundException()
        }

        // 4. 시즌 활성화 상태 확인
        if (season.status != SeasonStatus.ACTIVE) {
            throw SeasonNotActiveException()
        }

        // 5. 레슨 존재 및 해당 시즌 소속 확인
        val lesson =
            lessonRepository.findByUuid(lessonUuid)
                ?: throw LessonNotFoundException()

        if (lesson.seasonId != season.id) {
            throw LessonNotFoundException()
        }

        // 6. 레슨 상태 확인 (SCHEDULED만 취소 가능)
        if (lesson.status != LessonStatus.SCHEDULED) {
            throw LessonNotScheduledException()
        }

        // 7. 레슨 상태 → CANCELLED 변경
        val now = Instant.now()
        val cancelledLesson =
            lesson.copy(
                status = LessonStatus.CANCELLED,
                currentCount = 0,
                updatedAt = now,
            )

        lessonRepository.save(cancelledLesson)

        // TODO: Phase 6에서 예약자 환불 처리 추가
        // - 모든 RESERVED 예약 → CANCELLED_BY_INSTRUCTOR
        // - 티켓 환불 (TicketLog REFUND)
    }
}
