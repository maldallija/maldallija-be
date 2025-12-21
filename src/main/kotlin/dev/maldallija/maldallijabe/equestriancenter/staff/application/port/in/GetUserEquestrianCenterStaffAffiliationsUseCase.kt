package dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`

import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.dto.UserEquestrianCenterStaffAffiliationDetail
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface GetUserEquestrianCenterStaffAffiliationsUseCase {
    fun getUserEquestrianCenterStaffAffiliations(
        userUuid: UUID,
        requestingUserId: Long,
        pageable: Pageable,
    ): Page<UserEquestrianCenterStaffAffiliationDetail>
}
