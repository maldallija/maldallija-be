package dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out

import dev.maldallija.maldallijabe.equestriancenter.staff.domain.EquestrianCenterStaff
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface EquestrianCenterStaffRepository {
    fun existsByEquestrianCenterIdAndUserIdAndLeftAtIsNull(
        equestrianCenterId: Long,
        userId: Long,
    ): Boolean

    fun findActiveByEquestrianCenterIdAndUserId(
        equestrianCenterId: Long,
        userId: Long,
    ): EquestrianCenterStaff?

    fun findByUuid(uuid: UUID): EquestrianCenterStaff?

    fun findByEquestrianCenterIdAndLeftAtIsNull(
        equestrianCenterId: Long,
        pageable: Pageable,
    ): Page<EquestrianCenterStaff>

    fun findByUserIdAndLeftAtIsNull(
        userId: Long,
        pageable: Pageable,
    ): Page<EquestrianCenterStaff>

    fun save(equestrianCenterStaff: EquestrianCenterStaff): EquestrianCenterStaff

    fun findActiveByEquestrianCenterIdAndUuidIn(
        equestrianCenterId: Long,
        uuids: List<UUID>,
    ): List<EquestrianCenterStaff>
}
