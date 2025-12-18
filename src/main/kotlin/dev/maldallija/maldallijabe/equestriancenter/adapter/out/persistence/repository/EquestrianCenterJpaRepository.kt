package dev.maldallija.maldallijabe.equestriancenter.adapter.out.persistence.repository

import dev.maldallija.maldallijabe.equestriancenter.adapter.out.persistence.entity.EquestrianCenterEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EquestrianCenterJpaRepository : JpaRepository<EquestrianCenterEntity, Long> {
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<EquestrianCenterEntity>

    fun findByUuidAndDeletedAtIsNull(uuid: UUID): EquestrianCenterEntity?
}
