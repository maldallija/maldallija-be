package dev.maldallija.maldallijabe.lesson.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.util.UUID

interface LessonJpaRepository : JpaRepository<LessonEntity, Long> {
    fun findByUuidAndDeletedAtIsNull(uuid: UUID): LessonEntity?
    @Query(
        """
        SELECT l FROM LessonEntity l
        WHERE l.seasonId = :seasonId
          AND l.deletedAt IS NULL
          AND (:lessonDate IS NULL OR l.lessonDate = :lessonDate)
          AND (:lessonStatus IS NULL OR l.status = :lessonStatus)
        ORDER BY l.lessonDate ASC, l.startTime ASC
        """,
    )
    fun findBySeasonIdAndFilters(
        seasonId: Long,
        lessonDate: LocalDate?,
        lessonStatus: String?,
    ): List<LessonEntity>

    @Query(
        """
        SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END
        FROM LessonEntity l
        WHERE l.seasonId = :seasonId
          AND l.deletedAt IS NULL
          AND l.status = 'SCHEDULED'
          AND (l.lessonDate < :startDate OR l.lessonDate > :endDate)
        """,
    )
    fun existsBySeasonIdAndScheduledLessonDateOutsideRange(
        seasonId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Boolean
}
