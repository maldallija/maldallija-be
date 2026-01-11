package dev.maldallija.maldallijabe.lesson.application.port.`in`

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

interface CreateLessonUseCase {
    fun createLesson(
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
    )
}
