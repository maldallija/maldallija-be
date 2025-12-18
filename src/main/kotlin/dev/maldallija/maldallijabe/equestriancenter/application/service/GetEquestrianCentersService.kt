package dev.maldallija.maldallijabe.equestriancenter.application.service

import dev.maldallija.maldallijabe.equestriancenter.application.port.`in`.GetEquestrianCentersUseCase
import dev.maldallija.maldallijabe.equestriancenter.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.domain.EquestrianCenter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetEquestrianCentersService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
) : GetEquestrianCentersUseCase {
    override fun getEquestrianCenters(pageable: Pageable): Page<EquestrianCenter> = equestrianCenterRepository.findAll(pageable)
}
