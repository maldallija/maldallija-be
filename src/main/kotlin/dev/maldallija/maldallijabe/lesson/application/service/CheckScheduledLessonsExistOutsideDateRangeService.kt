package dev.maldallija.maldallijabe.lesson.application.service

import dev.maldallija.maldallijabe.lesson.application.port.`in`.CheckScheduledLessonsExistOutsideDateRangeUseCase
import dev.maldallija.maldallijabe.lesson.application.port.out.LessonRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class CheckScheduledLessonsExistOutsideDateRangeService(
    private val lessonRepository: LessonRepository,
) : CheckScheduledLessonsExistOutsideDateRangeUseCase {
    override fun existsOutsideDateRange(
        seasonId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Boolean =
        lessonRepository.existsBySeasonIdAndScheduledLessonDateOutsideRange(
            seasonId = seasonId,
            startDate = startDate,
            endDate = endDate,
        )
}
