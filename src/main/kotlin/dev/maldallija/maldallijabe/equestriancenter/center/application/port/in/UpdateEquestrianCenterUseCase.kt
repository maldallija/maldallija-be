package dev.maldallija.maldallijabe.equestriancenter.center.application.port.`in`

import java.util.UUID

interface UpdateEquestrianCenterUseCase {
    fun updateEquestrianCenter(
        equestrianCenterUuid: UUID,
        representativeUserId: Long,
        name: String?,
        description: String?,
    )
}
