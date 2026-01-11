package dev.maldallija.maldallijabe.lesson.application.port.`in`.dto

import dev.maldallija.maldallijabe.lesson.domain.LessonStatus
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class LessonSummary(
    val uuid: UUID,
    val title: String,
    val description: String?,
    val lessonDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val capacity: Int,
    val currentCount: Int,
    val ridingCenter: String?,
    val status: LessonStatus,
    val instructors: List<InstructorInfo>,
)
