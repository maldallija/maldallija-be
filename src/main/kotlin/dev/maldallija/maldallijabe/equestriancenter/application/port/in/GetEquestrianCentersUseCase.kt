package dev.maldallija.maldallijabe.equestriancenter.application.port.`in`

import dev.maldallija.maldallijabe.equestriancenter.domain.EquestrianCenter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface GetEquestrianCentersUseCase {
    fun getEquestrianCenters(pageable: Pageable): Page<EquestrianCenter>
}
