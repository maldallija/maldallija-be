package dev.maldallija.maldallijabe.equestriancenter.center.adapter.out.persistence

import dev.maldallija.maldallijabe.equestriancenter.center.adapter.out.persistence.mapper.EquestrianCenterMapper
import dev.maldallija.maldallijabe.equestriancenter.center.adapter.out.persistence.repository.EquestrianCenterJpaRepository
import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.EquestrianCenter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class EquestrianCenterRepositoryAdapter(
    private val equestrianCenterJpaRepository: EquestrianCenterJpaRepository,
    private val equestrianCenterMapper: EquestrianCenterMapper,
) : EquestrianCenterRepository {
    override fun findAll(pageable: Pageable): Page<EquestrianCenter> =
        equestrianCenterJpaRepository
            .findAllByDeletedAtIsNull(pageable)
            .map { equestrianCenterMapper.toDomain(it) }

    override fun findByUuid(uuid: UUID): EquestrianCenter? =
        equestrianCenterJpaRepository.findByUuidAndDeletedAtIsNull(uuid)?.let {
            equestrianCenterMapper.toDomain(it)
        }

    override fun findById(id: Long): EquestrianCenter? =
        equestrianCenterJpaRepository.findByIdAndDeletedAtIsNull(id)?.let {
            equestrianCenterMapper.toDomain(it)
        }

    override fun findAllByIdIn(ids: List<Long>): List<EquestrianCenter> =
        equestrianCenterJpaRepository.findAllByIdInAndDeletedAtIsNull(ids).map {
            equestrianCenterMapper.toDomain(it)
        }

    override fun save(equestrianCenter: EquestrianCenter): EquestrianCenter {
        val entity = equestrianCenterMapper.toEntity(equestrianCenter)
        val savedEntity = equestrianCenterJpaRepository.save(entity)
        return equestrianCenterMapper.toDomain(savedEntity)
    }
}
