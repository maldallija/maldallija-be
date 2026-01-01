package dev.maldallija.maldallijabe.equestriancenter.center.adapter.out.persistence.repository

import dev.maldallija.maldallijabe.equestriancenter.center.adapter.out.persistence.entity.EquestrianCenterEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EquestrianCenterJpaRepository : JpaRepository<EquestrianCenterEntity, Long> {
    fun findByUuidAndDeletedAtIsNull(uuid: UUID): EquestrianCenterEntity?

    fun findByIdAndDeletedAtIsNull(id: Long): EquestrianCenterEntity?

    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<EquestrianCenterEntity>

    fun findAllByIdInAndDeletedAtIsNull(ids: List<Long>): List<EquestrianCenterEntity>
}
