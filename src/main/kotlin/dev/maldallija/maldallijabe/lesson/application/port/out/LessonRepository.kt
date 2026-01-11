package dev.maldallija.maldallijabe.lesson.application.port.out

import dev.maldallija.maldallijabe.lesson.domain.Lesson
import dev.maldallija.maldallijabe.lesson.domain.LessonStatus
import java.time.LocalDate
import java.util.UUID

interface LessonRepository {
    fun save(lesson: Lesson): Lesson

    fun findByUuid(uuid: UUID): Lesson?

    fun findBySeasonIdAndFilters(
        seasonId: Long,
        lessonDate: LocalDate?,
        lessonStatus: LessonStatus?,
    ): List<Lesson>

    fun existsBySeasonIdAndScheduledLessonDateOutsideRange(
        seasonId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Boolean
}
