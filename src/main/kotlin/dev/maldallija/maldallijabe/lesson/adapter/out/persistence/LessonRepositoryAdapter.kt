package dev.maldallija.maldallijabe.lesson.adapter.out.persistence

import dev.maldallija.maldallijabe.lesson.application.port.out.LessonRepository
import dev.maldallija.maldallijabe.lesson.domain.Lesson
import org.springframework.stereotype.Repository

@Repository
class LessonRepositoryAdapter(
    private val lessonJpaRepository: LessonJpaRepository,
    private val lessonMapper: LessonMapper,
) : LessonRepository {
    override fun save(lesson: Lesson): Lesson {
        val entity = lessonMapper.toEntity(lesson)
        val savedEntity = lessonJpaRepository.save(entity)
        return lessonMapper.toDomain(savedEntity)
    }
}
