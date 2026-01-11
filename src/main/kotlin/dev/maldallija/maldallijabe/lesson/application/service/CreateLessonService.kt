package dev.maldallija.maldallijabe.lesson.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.lesson.application.port.`in`.CreateLessonUseCase
import dev.maldallija.maldallijabe.lesson.application.port.out.LessonRepository
import dev.maldallija.maldallijabe.lesson.domain.Lesson
import dev.maldallija.maldallijabe.lesson.domain.LessonStatus
import dev.maldallija.maldallijabe.lesson.domain.exception.AtLeastOneInstructorRequiredException
import dev.maldallija.maldallijabe.lesson.domain.exception.DuplicateInstructorException
import dev.maldallija.maldallijabe.lesson.domain.exception.InstructorNotBelongToCenterException
import dev.maldallija.maldallijabe.lesson.domain.exception.InvalidLessonCapacityException
import dev.maldallija.maldallijabe.lesson.domain.exception.InvalidLessonTimeException
import dev.maldallija.maldallijabe.lesson.domain.exception.InvalidLessonTitleException
import dev.maldallija.maldallijabe.lesson.domain.exception.LessonNotInSeasonPeriodException
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
class CreateLessonService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
    private val seasonRepository: SeasonRepository,
    private val lessonRepository: LessonRepository,
    private val lessonInstructorRepository: LessonInstructorRepository,
) : CreateLessonUseCase {
    override fun createLesson(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
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
        val requestingStaff =
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

        // 5. 입력값 검증
        if (title.isBlank()) {
            throw InvalidLessonTitleException()
        }
        if (!endTime.isAfter(startTime)) {
            throw InvalidLessonTimeException()
        }
        if (capacity < 1) {
            throw InvalidLessonCapacityException()
        }

        // 6. 레슨 날짜가 시즌 기간 내인지 확인
        if (lessonDate.isBefore(season.startDate) || lessonDate.isAfter(season.endDate)) {
            throw LessonNotInSeasonPeriodException()
        }

        // 7. 강사 목록 검증
        if (instructorStaffUuids.isEmpty()) {
            throw AtLeastOneInstructorRequiredException()
        }

        // 7-1. 중복 강사 검증
        if (instructorStaffUuids.size != instructorStaffUuids.distinct().size) {
            throw DuplicateInstructorException()
        }

        // 7-2. 강사가 해당 센터 소속인지 검증
        val instructorStaffList =
            equestrianCenterStaffRepository.findActiveByEquestrianCenterIdAndUuidIn(
                equestrianCenterId = equestrianCenter.id,
                uuids = instructorStaffUuids,
            )

        if (instructorStaffList.size != instructorStaffUuids.size) {
            throw InstructorNotBelongToCenterException()
        }

        // 8. 레슨 생성
        val now = Instant.now()
        val lesson =
            Lesson(
                id = 0L,
                uuid = UUID.randomUUID(),
                seasonId = season.id,
                title = title,
                description = description,
                lessonDate = lessonDate,
                startTime = startTime,
                endTime = endTime,
                capacity = capacity,
                currentCount = 0,
                ridingCenter = ridingCenter,
                status = LessonStatus.SCHEDULED,
                version = 0L,
                createdBy = requestingStaff.id,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
            )

        val savedLesson = lessonRepository.save(lesson)

        // 9. 강사 배정
        val lessonInstructors =
            instructorStaffList.map { staff ->
                LessonInstructor(
                    id = 0L,
                    lessonId = savedLesson.id,
                    staffId = staff.id,
                    createdAt = now,
                )
            }

        lessonInstructorRepository.saveAll(lessonInstructors)
    }
}
