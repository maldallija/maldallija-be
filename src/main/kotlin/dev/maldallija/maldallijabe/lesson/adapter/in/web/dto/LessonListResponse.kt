package dev.maldallija.maldallijabe.lesson.adapter.`in`.web.dto

import dev.maldallija.maldallijabe.lesson.domain.LessonStatus
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class LessonListResponse(
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
    val instructors: List<InstructorResponse>,
)

data class InstructorResponse(
    val staffUuid: UUID,
    val name: String,
)
