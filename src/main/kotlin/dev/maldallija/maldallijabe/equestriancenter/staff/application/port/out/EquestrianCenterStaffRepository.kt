package dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out

import dev.maldallija.maldallijabe.equestriancenter.staff.domain.EquestrianCenterStaff
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface EquestrianCenterStaffRepository {
    fun existsByEquestrianCenterIdAndUserIdAndLeftAtIsNull(
        equestrianCenterId: Long,
        userId: Long,
    ): Boolean

    fun findByEquestrianCenterIdAndLeftAtIsNull(
        equestrianCenterId: Long,
        pageable: Pageable,
    ): Page<EquestrianCenterStaff>

    fun save(equestrianCenterStaff: EquestrianCenterStaff): EquestrianCenterStaff
}
