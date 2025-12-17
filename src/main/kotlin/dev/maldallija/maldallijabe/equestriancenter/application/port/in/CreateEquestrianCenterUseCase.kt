package dev.maldallija.maldallijabe.equestriancenter.application.port.`in`

import java.util.UUID

interface CreateEquestrianCenterUseCase {
    fun createEquestrianCenter(
        name: String,
        description: String?,
        leaderUserUuid: UUID,
        requestingUserId: Long,
    )
}
