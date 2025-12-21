package dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`

import java.util.UUID

interface LeaveEquestrianCenterStaffUseCase {
    fun leaveEquestrianCenterStaff(
        equestrianCenterUuid: UUID,
        staffUuid: UUID,
        requestingUserId: Long,
    )
}
