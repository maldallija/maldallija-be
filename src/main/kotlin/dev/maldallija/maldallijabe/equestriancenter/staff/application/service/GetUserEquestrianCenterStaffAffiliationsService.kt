package dev.maldallija.maldallijabe.equestriancenter.staff.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.GetUserEquestrianCenterStaffAffiliationsUseCase
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.dto.UserEquestrianCenterStaffAffiliationDetail
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import dev.maldallija.maldallijabe.user.domain.exception.UnauthorizedUserOperationException
import dev.maldallija.maldallijabe.user.domain.exception.UserNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetUserEquestrianCenterStaffAffiliationsService(
    private val userRepository: UserRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
    private val equestrianCenterRepository: EquestrianCenterRepository,
) : GetUserEquestrianCenterStaffAffiliationsUseCase {
    override fun getUserEquestrianCenterStaffAffiliations(
        userUuid: UUID,
        requestingUserId: Long,
        pageable: Pageable,
    ): Page<UserEquestrianCenterStaffAffiliationDetail> {
        // 1. User 존재 확인
        val user =
            userRepository.findByUuid(userUuid)
                ?: throw UserNotFoundException()

        // 2. 본인인지 확인
        if (user.id != requestingUserId) {
            throw UnauthorizedUserOperationException()
        }

        // 3. Staff 목록 조회 (leftAt IS NULL)
        val staffPage =
            equestrianCenterStaffRepository.findByUserIdAndLeftAtIsNull(
                userId = user.id,
                pageable = pageable,
            )

        // 4. N+1 방지: EquestrianCenter batch fetch
        val equestrianCenterIds = staffPage.content.map { it.equestrianCenterId }
        val equestrianCentersById =
            if (equestrianCenterIds.isEmpty()) {
                emptyMap()
            } else {
                equestrianCenterRepository.findAllByIdIn(equestrianCenterIds).associateBy { it.id }
            }

        // 5. DTO 변환
        return staffPage.map { staff ->
            val equestrianCenter = equestrianCentersById[staff.equestrianCenterId]!!
            UserEquestrianCenterStaffAffiliationDetail(
                staffUuid = staff.uuid,
                equestrianCenterId = equestrianCenter.id,
                equestrianCenterUuid = equestrianCenter.uuid,
                equestrianCenterName = equestrianCenter.name,
                joinedAt = staff.joinedAt,
            )
        }
    }
}
