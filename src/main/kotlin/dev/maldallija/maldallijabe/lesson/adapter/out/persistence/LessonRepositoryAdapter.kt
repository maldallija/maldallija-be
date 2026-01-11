package dev.maldallija.maldallijabe.lesson.adapter.out.persistence

import dev.maldallija.maldallijabe.lesson.application.port.out.LessonRepository
import dev.maldallija.maldallijabe.lesson.domain.Lesson
import dev.maldallija.maldallijabe.lesson.domain.LessonStatus
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

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

    override fun findByUuid(uuid: UUID): Lesson? =
        lessonJpaRepository
            .findByUuidAndDeletedAtIsNull(uuid)
            ?.let { lessonMapper.toDomain(it) }

    override fun findBySeasonIdAndFilters(
        seasonId: Long,
        lessonDate: LocalDate?,
        lessonStatus: LessonStatus?,
    ): List<Lesson> =
        lessonJpaRepository
            .findBySeasonIdAndFilters(
                seasonId = seasonId,
                lessonDate = lessonDate,
                lessonStatus = lessonStatus?.name,
            ).map { lessonMapper.toDomain(it) }
}
