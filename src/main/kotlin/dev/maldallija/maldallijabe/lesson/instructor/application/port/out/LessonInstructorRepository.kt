package dev.maldallija.maldallijabe.lesson.instructor.application.port.out

import dev.maldallija.maldallijabe.lesson.instructor.domain.LessonInstructor

interface LessonInstructorRepository {
    fun saveAll(lessonInstructors: List<LessonInstructor>): List<LessonInstructor>
}
