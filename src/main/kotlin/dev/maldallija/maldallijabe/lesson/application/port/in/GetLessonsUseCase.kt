package dev.maldallija.maldallijabe.lesson.application.port.`in`

import dev.maldallija.maldallijabe.lesson.application.port.`in`.dto.LessonSummary
import dev.maldallija.maldallijabe.lesson.domain.LessonStatus
import java.time.LocalDate
import java.util.UUID

interface GetLessonsUseCase {
    fun getLessons(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        lessonDate: LocalDate?,
        lessonStatus: LessonStatus?,
    ): List<LessonSummary>
}
