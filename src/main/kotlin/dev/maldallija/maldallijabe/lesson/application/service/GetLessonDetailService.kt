package dev.maldallija.maldallijabe.lesson.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.lesson.application.port.`in`.GetLessonDetailUseCase
import dev.maldallija.maldallijabe.lesson.application.port.`in`.dto.InstructorInfo
import dev.maldallija.maldallijabe.lesson.application.port.`in`.dto.LessonSummary
import dev.maldallija.maldallijabe.lesson.application.port.out.LessonRepository
import dev.maldallija.maldallijabe.lesson.domain.exception.LessonNotFoundException
import dev.maldallija.maldallijabe.lesson.instructor.application.port.out.LessonInstructorRepository
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetLessonDetailService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val seasonRepository: SeasonRepository,
    private val lessonRepository: LessonRepository,
    private val lessonInstructorRepository: LessonInstructorRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
    private val userRepository: UserRepository,
) : GetLessonDetailUseCase {
    override fun getLessonDetail(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        lessonUuid: UUID,
    ): LessonSummary {
        // 1. 승마장 존재 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 2. 시즌 존재 확인
        val season =
            seasonRepository.findByUuid(seasonUuid)
                ?: throw SeasonNotFoundException()

        // 3. 시즌이 해당 승마장에 속하는지 검증
        if (season.equestrianCenterId != equestrianCenter.id) {
            throw SeasonNotFoundException()
        }

        // 4. 레슨 조회
        val lesson =
            lessonRepository.findByUuid(lessonUuid)
                ?: throw LessonNotFoundException()

        // 5. 레슨이 해당 시즌에 속하는지 검증
        if (lesson.seasonId != season.id) {
            throw LessonNotFoundException()
        }

        // 6. 강사 정보 조회
        val lessonInstructors = lessonInstructorRepository.findByLessonIdIn(listOf(lesson.id))

        val staffIds = lessonInstructors.map { it.staffId }.distinct()
        val staffMap =
            if (staffIds.isEmpty()) {
                emptyMap()
            } else {
                equestrianCenterStaffRepository.findAllByIdIn(staffIds).associateBy { it.id }
            }

        val userIds = staffMap.values.map { it.userId }.distinct()
        val userMap =
            if (userIds.isEmpty()) {
                emptyMap()
            } else {
                userRepository.findAllByIdIn(userIds).associateBy { it.id }
            }

        val instructors =
            lessonInstructors.mapNotNull { lessonInstructor ->
                val staff = staffMap[lessonInstructor.staffId]
                val user = staff?.let { userMap[it.userId] }
                if (staff != null && user != null) {
                    InstructorInfo(
                        staffUuid = staff.uuid,
                        name = user.nickname,
                    )
                } else {
                    null
                }
            }

        // 7. 응답 반환
        return LessonSummary(
            uuid = lesson.uuid,
            title = lesson.title,
            description = lesson.description,
            lessonDate = lesson.lessonDate,
            startTime = lesson.startTime,
            endTime = lesson.endTime,
            capacity = lesson.capacity,
            currentCount = lesson.currentCount,
            ridingCenter = lesson.ridingCenter,
            status = lesson.status,
            instructors = instructors,
        )
    }
}
