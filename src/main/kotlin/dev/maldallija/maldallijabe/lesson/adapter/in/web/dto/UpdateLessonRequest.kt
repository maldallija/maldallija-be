package dev.maldallija.maldallijabe.lesson.adapter.`in`.web.dto

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class UpdateLessonRequest(
    val title: String,
    val description: String?,
    val lessonDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val capacity: Int,
    val ridingCenter: String?,
    val instructorStaffUuids: List<UUID>,
)
