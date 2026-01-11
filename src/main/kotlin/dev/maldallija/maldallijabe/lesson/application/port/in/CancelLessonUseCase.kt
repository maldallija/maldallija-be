package dev.maldallija.maldallijabe.lesson.application.port.`in`

import java.util.UUID

interface CancelLessonUseCase {
    fun cancelLesson(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        lessonUuid: UUID,
        requestingUserId: Long,
    )
}
