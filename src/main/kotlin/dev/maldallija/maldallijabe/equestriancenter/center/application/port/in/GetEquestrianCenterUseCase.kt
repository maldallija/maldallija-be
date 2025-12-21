package dev.maldallija.maldallijabe.equestriancenter.center.application.port.`in`

import java.util.UUID

interface GetEquestrianCenterUseCase {
    fun getEquestrianCenter(equestrianCenterUuid: UUID): EquestrianCenterDetail
}
