package dev.maldallija.maldallijabe.equestriancenter.application.port.out

import dev.maldallija.maldallijabe.equestriancenter.domain.EquestrianCenter
import java.util.UUID

interface EquestrianCenterRepository {
    fun findByUuid(uuid: UUID): EquestrianCenter?

    fun save(equestrianCenter: EquestrianCenter): EquestrianCenter
}
