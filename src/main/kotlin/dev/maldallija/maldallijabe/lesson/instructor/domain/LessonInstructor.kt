package dev.maldallija.maldallijabe.lesson.instructor.domain

import java.time.Instant

data class LessonInstructor(
    val id: Long,
    val lessonId: Long,
    val staffId: Long,
    val createdAt: Instant,
)
