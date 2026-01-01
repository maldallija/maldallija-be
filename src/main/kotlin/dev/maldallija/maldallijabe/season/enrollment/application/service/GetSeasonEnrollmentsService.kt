package dev.maldallija.maldallijabe.season.enrollment.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import dev.maldallija.maldallijabe.season.domain.exception.UnauthorizedSeasonOperationException
import dev.maldallija.maldallijabe.season.enrollment.application.port.`in`.GetSeasonEnrollmentsUseCase
import dev.maldallija.maldallijabe.season.enrollment.application.port.`in`.dto.SeasonEnrollmentDetail
import dev.maldallija.maldallijabe.season.enrollment.application.port.out.SeasonEnrollmentRepository
import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetSeasonEnrollmentsService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val seasonRepository: SeasonRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
    private val seasonEnrollmentRepository: SeasonEnrollmentRepository,
    private val userRepository: UserRepository,
) : GetSeasonEnrollmentsUseCase {
    override fun getSeasonEnrollments(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        requestingUserId: Long,
        enrollmentStatus: EnrollmentStatus?,
        pageable: Pageable,
    ): Page<SeasonEnrollmentDetail> {
        // 1. 승마장 존재 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 2. 시즌 조회
        val season =
            seasonRepository.findByUuid(seasonUuid)
                ?: throw SeasonNotFoundException()

        // 3. 시즌이 해당 승마장에 속하는지 검증
        if (season.equestrianCenterId != equestrianCenter.id) {
            throw SeasonNotFoundException()
        }

        // 4. 직원 권한 확인
        equestrianCenterStaffRepository.findActiveByEquestrianCenterIdAndUserId(
            equestrianCenterId = equestrianCenter.id,
            userId = requestingUserId,
        ) ?: throw UnauthorizedSeasonOperationException()

        // 5. Enrollment 목록 조회
        val enrollmentPage =
            seasonEnrollmentRepository.findBySeasonIdAndOptionalStatus(
                seasonId = season.id,
                enrollmentStatus = enrollmentStatus,
                pageable = pageable,
            )

        // 6. Member 정보 batch fetch (N+1 방지)
        val memberIds = enrollmentPage.content.map { it.memberId }
        val members =
            if (memberIds.isEmpty()) {
                emptyMap()
            } else {
                userRepository.findAllByIdIn(memberIds).associateBy { it.id }
            }

        // 7. DTO 변환
        return enrollmentPage.map { enrollment ->
            val member = members[enrollment.memberId]!!
            SeasonEnrollmentDetail(
                enrollmentUuid = enrollment.uuid,
                memberUuid = member.uuid,
                memberNickname = member.nickname,
                enrollmentStatus = enrollment.enrollmentStatus,
                createdAt = enrollment.createdAt,
            )
        }
    }
}
