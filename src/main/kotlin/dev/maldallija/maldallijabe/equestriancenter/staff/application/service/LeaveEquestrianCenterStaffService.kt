package dev.maldallija.maldallijabe.equestriancenter.staff.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.LeaveEquestrianCenterStaffUseCase
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.equestriancenter.staff.domain.StaffLeftReason
import dev.maldallija.maldallijabe.equestriancenter.staff.domain.exception.StaffAlreadyLeftException
import dev.maldallija.maldallijabe.equestriancenter.staff.domain.exception.StaffNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.domain.exception.UnauthorizedStaffOperationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class LeaveEquestrianCenterStaffService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
) : LeaveEquestrianCenterStaffUseCase {
    override fun leaveEquestrianCenterStaff(
        equestrianCenterUuid: UUID,
        staffUuid: UUID,
        requestingUserId: Long,
    ) {
        // 1. EquestrianCenter 존재 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 2. Staff 존재 확인
        val staff =
            equestrianCenterStaffRepository.findByUuid(staffUuid)
                ?: throw StaffNotFoundException()

        // 3. Staff가 해당 center 소속인지 확인
        if (staff.equestrianCenterId != equestrianCenter.id) {
            throw StaffNotFoundException()
        }

        // 4. 본인인지 확인
        if (staff.userId != requestingUserId) {
            throw UnauthorizedStaffOperationException()
        }

        // 5. 이미 퇴사한 직원인지 확인
        if (staff.leftAt != null) {
            throw StaffAlreadyLeftException()
        }

        // 6. 탈퇴 처리
        val now = Instant.now()
        val leftStaff =
            staff.copy(
                leftAt = now,
                leftBy = null,
                leftReason = StaffLeftReason.LEFT_VOLUNTARILY,
                updatedAt = now,
                updatedBy = requestingUserId,
            )

        equestrianCenterStaffRepository.save(leftStaff)
    }
}
