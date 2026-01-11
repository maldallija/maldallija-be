package dev.maldallija.maldallijabe.lesson.adapter.out.persistence

import dev.maldallija.maldallijabe.lesson.domain.Lesson
import org.springframework.stereotype.Component

@Component
class LessonMapper {
    fun toDomain(entity: LessonEntity): Lesson =
        Lesson(
            id = entity.id,
            uuid = entity.uuid,
            seasonId = entity.seasonId,
            title = entity.title,
            description = entity.description,
            lessonDate = entity.lessonDate,
            startTime = entity.startTime,
            endTime = entity.endTime,
            capacity = entity.capacity,
            currentCount = entity.currentCount,
            ridingCenter = entity.ridingCenter,
            status = entity.status,
            version = entity.version,
            createdBy = entity.createdBy,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            deletedAt = entity.deletedAt,
        )

    fun toEntity(domain: Lesson): LessonEntity =
        LessonEntity(
            id = domain.id,
            uuid = domain.uuid,
            seasonId = domain.seasonId,
            title = domain.title,
            description = domain.description,
            lessonDate = domain.lessonDate,
            startTime = domain.startTime,
            endTime = domain.endTime,
            capacity = domain.capacity,
            currentCount = domain.currentCount,
            ridingCenter = domain.ridingCenter,
            status = domain.status,
            version = domain.version,
            createdBy = domain.createdBy,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            deletedAt = domain.deletedAt,
        )
}
