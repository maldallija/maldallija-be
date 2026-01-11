package dev.maldallija.maldallijabe.lesson.instructor.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface LessonInstructorJpaRepository : JpaRepository<LessonInstructorEntity, Long> {
    fun findByLessonIdIn(lessonIds: List<Long>): List<LessonInstructorEntity>
}
