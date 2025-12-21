package dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence

import dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence.repository.EquestrianCenterStaffJpaRepository
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import org.springframework.stereotype.Repository

@Repository
class EquestrianCenterStaffRepositoryAdapter(
    private val equestrianCenterStaffJpaRepository: EquestrianCenterStaffJpaRepository,
) : EquestrianCenterStaffRepository {
    override fun existsByEquestrianCenterIdAndUserIdAndLeftAtIsNull(
        equestrianCenterId: Long,
        userId: Long,
    ): Boolean =
        equestrianCenterStaffJpaRepository.existsByEquestrianCenterIdAndUserIdAndLeftAtIsNull(
            equestrianCenterId = equestrianCenterId,
            userId = userId,
        )
}
