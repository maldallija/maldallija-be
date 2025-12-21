package dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`

import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.dto.UserEquestrianCenterStaffMembershipDetail
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface GetUserEquestrianCenterStaffMembershipsUseCase {
    fun getUserEquestrianCenterStaffMemberships(
        userUuid: UUID,
        requestingUserId: Long,
        pageable: Pageable,
    ): Page<UserEquestrianCenterStaffMembershipDetail>
}
