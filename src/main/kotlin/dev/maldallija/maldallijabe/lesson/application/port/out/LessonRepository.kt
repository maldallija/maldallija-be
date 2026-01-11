package dev.maldallija.maldallijabe.lesson.application.port.out

import dev.maldallija.maldallijabe.lesson.domain.Lesson
import dev.maldallija.maldallijabe.lesson.domain.LessonStatus
import java.time.LocalDate

interface LessonRepository {
    fun save(lesson: Lesson): Lesson

    fun findBySeasonIdAndFilters(
        seasonId: Long,
        lessonDate: LocalDate?,
        lessonStatus: LessonStatus?,
    ): List<Lesson>
}
