package dev.maldallija.maldallijabe.lesson.application.port.`in`

import java.time.LocalDate

interface CheckScheduledLessonsExistOutsideDateRangeUseCase {
    fun existsOutsideDateRange(
        seasonId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Boolean
}
