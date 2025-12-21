package dev.maldallija.maldallijabe.equestriancenter.staff.application.service

import dev.maldallija.maldallijabe.equestriancenter.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.UnauthorizedEquestrianCenterOperationException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.ExpelEquestrianCenterStaffUseCase
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.equestriancenter.staff.domain.StaffLeftReason
import dev.maldallija.maldallijabe.equestriancenter.staff.domain.exception.StaffAlreadyLeftException
import dev.maldallija.maldallijabe.equestriancenter.staff.domain.exception.StaffNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class ExpelEquestrianCenterStaffService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
) : ExpelEquestrianCenterStaffUseCase {
    override fun expelEquestrianCenterStaff(
        equestrianCenterUuid: UUID,
        staffUuid: UUID,
        requestingUserId: Long,
    ) {
        // 1. EquestrianCenter 존재 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 2. 요청자가 대표인지 확인
        if (equestrianCenter.representativeUserId != requestingUserId) {
            throw UnauthorizedEquestrianCenterOperationException()
        }

        // 3. Staff 존재 확인
        val staff =
            equestrianCenterStaffRepository.findByUuid(staffUuid)
                ?: throw StaffNotFoundException()

        // 4. Staff가 해당 center 소속인지 확인
        if (staff.equestrianCenterId != equestrianCenter.id) {
            throw StaffNotFoundException()
        }

        // 5. 이미 퇴사한 직원인지 확인
        if (staff.leftAt != null) {
            throw StaffAlreadyLeftException()
        }

        // 6. 추방 처리
        val now = Instant.now()
        val expelledStaff =
            staff.copy(
                leftAt = now,
                leftBy = requestingUserId,
                leftReason = StaffLeftReason.EXPELLED,
                updatedAt = now,
                updatedBy = requestingUserId,
            )

        equestrianCenterStaffRepository.save(expelledStaff)
    }
}
