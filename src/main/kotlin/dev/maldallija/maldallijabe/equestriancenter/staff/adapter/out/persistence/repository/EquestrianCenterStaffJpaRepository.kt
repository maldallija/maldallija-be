package dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence.repository

import dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence.entity.EquestrianCenterStaffEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EquestrianCenterStaffJpaRepository : JpaRepository<EquestrianCenterStaffEntity, Long> {
    fun existsByEquestrianCenterIdAndUserIdAndLeftAtIsNull(
        equestrianCenterId: Long,
        userId: Long,
    ): Boolean

    fun findByUuidAndDeletedAtIsNull(uuid: UUID): EquestrianCenterStaffEntity?

    fun findByEquestrianCenterIdAndLeftAtIsNullAndDeletedAtIsNull(
        equestrianCenterId: Long,
        pageable: Pageable,
    ): Page<EquestrianCenterStaffEntity>

    fun findByUserIdAndLeftAtIsNullAndDeletedAtIsNull(
        userId: Long,
        pageable: Pageable,
    ): Page<EquestrianCenterStaffEntity>
}
