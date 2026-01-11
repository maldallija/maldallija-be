package dev.maldallija.maldallijabe.lesson.application.port.out

import dev.maldallija.maldallijabe.lesson.domain.Lesson

interface LessonRepository {
    fun save(lesson: Lesson): Lesson
}
