package dev.maldallija.maldallijabe.lesson.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class Lesson(
    val id: Long,
    val uuid: UUID,
    val seasonId: Long,
    val title: String,
    val description: String?,
    val lessonDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val capacity: Int,
    val currentCount: Int,
    val ridingCenter: String?,
    val status: LessonStatus,
    val version: Long,
    val createdBy: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
)
