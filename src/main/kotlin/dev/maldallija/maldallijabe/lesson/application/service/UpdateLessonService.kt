package dev.maldallija.maldallijabe.lesson.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.lesson.application.port.`in`.UpdateLessonUseCase
import dev.maldallija.maldallijabe.lesson.application.port.out.LessonRepository
import dev.maldallija.maldallijabe.lesson.domain.LessonStatus
import dev.maldallija.maldallijabe.lesson.domain.exception.AtLeastOneInstructorRequiredException
import dev.maldallija.maldallijabe.lesson.domain.exception.CapacityLessThanCurrentCountException
import dev.maldallija.maldallijabe.lesson.domain.exception.DuplicateInstructorException
import dev.maldallija.maldallijabe.lesson.domain.exception.InstructorNotBelongToCenterException
import dev.maldallija.maldallijabe.lesson.domain.exception.InvalidLessonCapacityException
import dev.maldallija.maldallijabe.lesson.domain.exception.InvalidLessonTimeException
import dev.maldallija.maldallijabe.lesson.domain.exception.InvalidLessonTitleException
import dev.maldallija.maldallijabe.lesson.domain.exception.LessonNotFoundException
import dev.maldallija.maldallijabe.lesson.domain.exception.LessonNotInSeasonPeriodException
import dev.maldallija.maldallijabe.lesson.domain.exception.LessonNotScheduledException
import dev.maldallija.maldallijabe.lesson.domain.exception.SeasonNotActiveException
import dev.maldallija.maldallijabe.lesson.domain.exception.UnauthorizedLessonOperationException
import dev.maldallija.maldallijabe.lesson.instructor.application.port.out.LessonInstructorRepository
import dev.maldallija.maldallijabe.lesson.instructor.domain.LessonInstructor
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Service
@Transactional
class UpdateLessonService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
    private val seasonRepository: SeasonRepository,
    private val lessonRepository: LessonRepository,
    private val lessonInstructorRepository: LessonInstructorRepository,
) : UpdateLessonUseCase {
    override fun updateLesson(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        lessonUuid: UUID,
        requestingUserId: Long,
        title: String,
        description: String?,
        lessonDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        capacity: Int,
        ridingCenter: String?,
        instructorStaffUuids: List<UUID>,
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

        // 6. 레슨 상태 확인 (SCHEDULED만 수정 가능)
        if (lesson.status != LessonStatus.SCHEDULED) {
            throw LessonNotScheduledException()
        }

        // 7. 입력값 검증
        if (title.isBlank()) {
            throw InvalidLessonTitleException()
        }
        if (!endTime.isAfter(startTime)) {
            throw InvalidLessonTimeException()
        }
        if (capacity < 1) {
            throw InvalidLessonCapacityException()
        }
        if (capacity < lesson.currentCount) {
            throw CapacityLessThanCurrentCountException()
        }

        // 8. 레슨 날짜가 시즌 기간 내인지 확인
        if (lessonDate.isBefore(season.startDate) || lessonDate.isAfter(season.endDate)) {
            throw LessonNotInSeasonPeriodException()
        }

        // 9. 강사 목록 검증
        if (instructorStaffUuids.isEmpty()) {
            throw AtLeastOneInstructorRequiredException()
        }

        if (instructorStaffUuids.size != instructorStaffUuids.distinct().size) {
            throw DuplicateInstructorException()
        }

        val instructorStaffList =
            equestrianCenterStaffRepository.findActiveByEquestrianCenterIdAndUuidIn(
                equestrianCenterId = equestrianCenter.id,
                uuids = instructorStaffUuids,
            )

        if (instructorStaffList.size != instructorStaffUuids.size) {
            throw InstructorNotBelongToCenterException()
        }

        // 10. 레슨 업데이트
        val now = Instant.now()
        val updatedLesson =
            lesson.copy(
                title = title,
                description = description,
                lessonDate = lessonDate,
                startTime = startTime,
                endTime = endTime,
                capacity = capacity,
                ridingCenter = ridingCenter,
                updatedAt = now,
            )

        lessonRepository.save(updatedLesson)

        // 11. 강사 목록 업데이트 (기존 삭제 후 새로 생성)
        lessonInstructorRepository.deleteByLessonId(lesson.id)

        val lessonInstructors =
            instructorStaffList.map { staff ->
                LessonInstructor(
                    id = 0L,
                    lessonId = lesson.id,
                    staffId = staff.id,
                    createdAt = now,
                )
            }

        lessonInstructorRepository.saveAll(lessonInstructors)
    }
}
