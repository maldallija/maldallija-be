package dev.maldallija.maldallijabe.lesson.instructor.adapter.out.persistence

import dev.maldallija.maldallijabe.lesson.instructor.application.port.out.LessonInstructorRepository
import dev.maldallija.maldallijabe.lesson.instructor.domain.LessonInstructor
import org.springframework.stereotype.Repository

@Repository
class LessonInstructorRepositoryAdapter(
    private val lessonInstructorJpaRepository: LessonInstructorJpaRepository,
    private val lessonInstructorMapper: LessonInstructorMapper,
) : LessonInstructorRepository {
    override fun saveAll(lessonInstructors: List<LessonInstructor>): List<LessonInstructor> {
        val entities = lessonInstructors.map { lessonInstructorMapper.toEntity(it) }
        val savedEntities = lessonInstructorJpaRepository.saveAll(entities)
        return savedEntities.map { lessonInstructorMapper.toDomain(it) }
    }

    override fun findByLessonIdIn(lessonIds: List<Long>): List<LessonInstructor> =
        lessonInstructorJpaRepository
            .findByLessonIdIn(lessonIds)
            .map { lessonInstructorMapper.toDomain(it) }

    override fun deleteByLessonId(lessonId: Long) {
        lessonInstructorJpaRepository.deleteByLessonId(lessonId)
    }
}
