package dev.maldallija.maldallijabe.equestriancenter.application.port.out

import dev.maldallija.maldallijabe.equestriancenter.domain.EquestrianCenter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface EquestrianCenterRepository {
    fun findAll(pageable: Pageable): Page<EquestrianCenter>

    fun findByUuid(uuid: UUID): EquestrianCenter?

    fun findAllByIdIn(ids: List<Long>): List<EquestrianCenter>

    fun save(equestrianCenter: EquestrianCenter): EquestrianCenter
}
