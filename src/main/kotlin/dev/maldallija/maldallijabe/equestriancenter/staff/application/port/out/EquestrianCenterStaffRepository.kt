package dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out

interface EquestrianCenterStaffRepository {
    fun existsByEquestrianCenterIdAndUserIdAndLeftAtIsNull(
        equestrianCenterId: Long,
        userId: Long,
    ): Boolean
}
