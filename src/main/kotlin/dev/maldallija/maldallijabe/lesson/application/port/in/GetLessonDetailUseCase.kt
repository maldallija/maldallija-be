package dev.maldallija.maldallijabe.lesson.application.port.`in`

import dev.maldallija.maldallijabe.lesson.application.port.`in`.dto.LessonSummary
import java.util.UUID

interface GetLessonDetailUseCase {
    fun getLessonDetail(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        lessonUuid: UUID,
    ): LessonSummary
}
