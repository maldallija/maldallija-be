package dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence

import dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence.mapper.EquestrianCenterStaffMapper
import dev.maldallija.maldallijabe.equestriancenter.staff.adapter.out.persistence.repository.EquestrianCenterStaffJpaRepository
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.equestriancenter.staff.domain.EquestrianCenterStaff
import org.springframework.stereotype.Repository

@Repository
class EquestrianCenterStaffRepositoryAdapter(
    private val equestrianCenterStaffJpaRepository: EquestrianCenterStaffJpaRepository,
    private val equestrianCenterStaffMapper: EquestrianCenterStaffMapper,
) : EquestrianCenterStaffRepository {
    override fun existsByEquestrianCenterIdAndUserIdAndLeftAtIsNull(
        equestrianCenterId: Long,
        userId: Long,
    ): Boolean =
        equestrianCenterStaffJpaRepository.existsByEquestrianCenterIdAndUserIdAndLeftAtIsNull(
            equestrianCenterId = equestrianCenterId,
            userId = userId,
        )

    override fun save(equestrianCenterStaff: EquestrianCenterStaff): EquestrianCenterStaff {
        val entity = equestrianCenterStaffMapper.toEntity(equestrianCenterStaff)
        val savedEntity = equestrianCenterStaffJpaRepository.save(entity)
        return equestrianCenterStaffMapper.toDomain(savedEntity)
    }
}
