package dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`

import java.util.UUID

interface ExpelEquestrianCenterStaffUseCase {
    fun expelEquestrianCenterStaff(
        equestrianCenterUuid: UUID,
        staffUuid: UUID,
        requestingUserId: Long,
    )
}
