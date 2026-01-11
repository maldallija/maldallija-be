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
}
