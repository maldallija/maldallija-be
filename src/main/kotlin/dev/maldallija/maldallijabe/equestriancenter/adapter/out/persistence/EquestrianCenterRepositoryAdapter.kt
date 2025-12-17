package dev.maldallija.maldallijabe.equestriancenter.adapter.out.persistence

import dev.maldallija.maldallijabe.equestriancenter.adapter.out.persistence.mapper.EquestrianCenterMapper
import dev.maldallija.maldallijabe.equestriancenter.adapter.out.persistence.repository.EquestrianCenterJpaRepository
import dev.maldallija.maldallijabe.equestriancenter.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.domain.EquestrianCenter
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class EquestrianCenterRepositoryAdapter(
    private val equestrianCenterJpaRepository: EquestrianCenterJpaRepository,
    private val equestrianCenterMapper: EquestrianCenterMapper,
) : EquestrianCenterRepository {
    override fun findByUuid(uuid: UUID): EquestrianCenter? =
        equestrianCenterJpaRepository.findByUuidAndDeletedAtIsNull(uuid)?.let {
            equestrianCenterMapper.toDomain(it)
        }

    override fun save(equestrianCenter: EquestrianCenter): EquestrianCenter {
        val entity = equestrianCenterMapper.toEntity(equestrianCenter)
        val savedEntity = equestrianCenterJpaRepository.save(entity)
        return equestrianCenterMapper.toDomain(savedEntity)
    }
}
