package dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`

import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.dto.EquestrianCenterStaffDetail
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface GetEquestrianCenterStaffUseCase {
    fun getEquestrianCenterStaff(
        equestrianCenterUuid: UUID,
        pageable: Pageable,
    ): Page<EquestrianCenterStaffDetail>
}
