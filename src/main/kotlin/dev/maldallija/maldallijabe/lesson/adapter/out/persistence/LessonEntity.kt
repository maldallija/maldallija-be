package dev.maldallija.maldallijabe.lesson.adapter.out.persistence

import dev.maldallija.maldallijabe.lesson.domain.LessonStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "lesson")
class LessonEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @Column(nullable = false, unique = true)
    val uuid: UUID,
    @Column(name = "season_id", nullable = false)
    val seasonId: Long,
    @Column(nullable = false, length = 200)
    val title: String,
    @Column(columnDefinition = "TEXT")
    val description: String?,
    @Column(name = "lesson_date", nullable = false)
    val lessonDate: LocalDate,
    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime,
    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime,
    @Column(nullable = false)
    val capacity: Int,
    @Column(name = "current_count", nullable = false)
    val currentCount: Int,
    @Column(name = "riding_center", length = 200)
    val ridingCenter: String?,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: LessonStatus,
    @Version
    @Column(nullable = false)
    val version: Long,
    @Column(name = "created_by", nullable = false)
    val createdBy: Long,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,
    @Column(name = "deleted_at")
    val deletedAt: Instant?,
)
