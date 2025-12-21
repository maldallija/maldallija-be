package dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out

import dev.maldallija.maldallijabe.equestriancenter.staff.domain.EquestrianCenterStaff

interface EquestrianCenterStaffRepository {
    fun existsByEquestrianCenterIdAndUserIdAndLeftAtIsNull(
        equestrianCenterId: Long,
        userId: Long,
    ): Boolean

    fun save(equestrianCenterStaff: EquestrianCenterStaff): EquestrianCenterStaff
}
