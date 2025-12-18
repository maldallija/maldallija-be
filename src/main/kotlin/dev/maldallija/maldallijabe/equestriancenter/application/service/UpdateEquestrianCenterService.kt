package dev.maldallija.maldallijabe.equestriancenter.application.service

import dev.maldallija.maldallijabe.equestriancenter.application.port.`in`.UpdateEquestrianCenterUseCase
import dev.maldallija.maldallijabe.equestriancenter.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.UnauthorizedEquestrianCenterOperationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class UpdateEquestrianCenterService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
) : UpdateEquestrianCenterUseCase {
    override fun updateEquestrianCenter(
        equestrianCenterUuid: UUID,
        representativeUserId: Long,
        name: String?,
        description: String?,
    ) {
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        if (equestrianCenter.representativeUserId != representativeUserId) {
            throw UnauthorizedEquestrianCenterOperationException()
        }

        val now = Instant.now()

        val updatedEquestrianCenter =
            equestrianCenter.copy(
                name = name ?: equestrianCenter.name,
                description = description ?: equestrianCenter.description,
                updatedBy = representativeUserId,
                updatedAt = now,
            )

        equestrianCenterRepository.save(updatedEquestrianCenter)
    }
}
