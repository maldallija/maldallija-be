package dev.maldallija.maldallijabe.equestriancenter.staff.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.GetEquestrianCenterStaffUseCase
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.dto.EquestrianCenterStaffDetail
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetEquestrianCenterStaffService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
    private val userRepository: UserRepository,
) : GetEquestrianCenterStaffUseCase {
    override fun getEquestrianCenterStaff(
        equestrianCenterUuid: UUID,
        pageable: Pageable,
    ): Page<EquestrianCenterStaffDetail> {
        // 1. EquestrianCenter 존재 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 2. Staff 목록 조회 (leftAt IS NULL)
        val staffPage =
            equestrianCenterStaffRepository.findByEquestrianCenterIdAndLeftAtIsNull(
                equestrianCenterId = equestrianCenter.id,
                pageable = pageable,
            )

        // 3. N+1 방지: User batch fetch
        val userIds = staffPage.content.map { it.userId }
        val usersById =
            if (userIds.isEmpty()) {
                emptyMap()
            } else {
                userRepository.findAllByIdIn(userIds).associateBy { it.id }
            }

        // 4. DTO 변환
        return staffPage.map { staff ->
            val user = usersById[staff.userId]!!
            EquestrianCenterStaffDetail(
                staffUuid = staff.uuid,
                userId = user.id,
                userUuid = user.uuid,
                userNickname = user.nickname,
                joinedAt = staff.joinedAt,
            )
        }
    }
}
