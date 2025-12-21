package dev.maldallija.maldallijabe.equestriancenter.center.application.port.`in`

import java.util.UUID

interface CreateEquestrianCenterUseCase {
    fun createEquestrianCenter(
        administratorId: Long,
        name: String,
        description: String?,
        representativeUserUuid: UUID,
    )
}
