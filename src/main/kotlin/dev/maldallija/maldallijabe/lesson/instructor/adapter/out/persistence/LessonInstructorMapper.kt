package dev.maldallija.maldallijabe.lesson.instructor.adapter.out.persistence

import dev.maldallija.maldallijabe.lesson.instructor.domain.LessonInstructor
import org.springframework.stereotype.Component

@Component
class LessonInstructorMapper {
    fun toDomain(entity: LessonInstructorEntity): LessonInstructor =
        LessonInstructor(
            id = entity.id,
            lessonId = entity.lessonId,
            staffId = entity.staffId,
            createdAt = entity.createdAt,
        )

    fun toEntity(domain: LessonInstructor): LessonInstructorEntity =
        LessonInstructorEntity(
            id = domain.id,
            lessonId = domain.lessonId,
            staffId = domain.staffId,
            createdAt = domain.createdAt,
        )
}
