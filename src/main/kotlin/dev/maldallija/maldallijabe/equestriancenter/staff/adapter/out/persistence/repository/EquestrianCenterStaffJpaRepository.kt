package dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence.repository

import dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence.entity.EquestrianCenterStaffEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EquestrianCenterStaffJpaRepository : JpaRepository<EquestrianCenterStaffEntity, Long> {
    fun existsByEquestrianCenterIdAndUserIdAndLeftAtIsNull(
        equestrianCenterId: Long,
        userId: Long,
    ): Boolean
}
