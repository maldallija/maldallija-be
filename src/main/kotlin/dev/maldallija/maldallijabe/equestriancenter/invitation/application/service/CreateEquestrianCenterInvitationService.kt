package dev.maldallija.maldallijabe.equestriancenter.invitation.application.service

import dev.maldallija.maldallijabe.equestriancenter.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.UnauthorizedEquestrianCenterOperationException
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.CreateEquestrianCenterInvitationUseCase
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.out.EquestrianCenterInvitationRepository
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.EquestrianCenterInvitation
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception.AlreadyStaffMemberException
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception.DuplicateInvitationException
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception.SelfInvitationException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import dev.maldallija.maldallijabe.user.domain.exception.UserNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class CreateEquestrianCenterInvitationService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val userRepository: UserRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
    private val equestrianCenterInvitationRepository: EquestrianCenterInvitationRepository,
) : CreateEquestrianCenterInvitationUseCase {
    override fun createEquestrianCenterInvitation(
        equestrianCenterUuid: UUID,
        requestingUserId: Long,
        invitedUserUuid: UUID,
    ) {
        // 1. 센터 존재 & 삭제 안됨 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 2. 대표 확인
        if (equestrianCenter.representativeUserId != requestingUserId) {
            throw UnauthorizedEquestrianCenterOperationException()
        }

        // 3. 초대 대상 존재 & 삭제 안됨 확인
        val invitedUser =
            userRepository.findByUuid(invitedUserUuid)
                ?: throw UserNotFoundException()

        // 4. 자기 자신 초대 방지
        if (invitedUser.id == requestingUserId) {
            throw SelfInvitationException()
        }

        // 5. 이미 활성 staff 체크
        val isActiveStaff =
            equestrianCenterStaffRepository.existsByEquestrianCenterIdAndUserIdAndLeftAtIsNull(
                equestrianCenterId = equestrianCenter.id,
                userId = invitedUser.id,
            )
        if (isActiveStaff) {
            throw AlreadyStaffMemberException()
        }

        // 6. 이미 INVITED 상태 초대 존재 체크
        val hasPendingInvitation =
            equestrianCenterInvitationRepository.existsByEquestrianCenterIdAndUserIdAndStatus(
                equestrianCenterId = equestrianCenter.id,
                userId = invitedUser.id,
                status = InvitationStatus.INVITED,
            )
        if (hasPendingInvitation) {
            throw DuplicateInvitationException()
        }

        // 7. 초대 생성
        val now = Instant.now()
        val expiresAt = now.plusSeconds(7 * 24 * 60 * 60) // 7일

        val equestrianCenterInvitation =
            EquestrianCenterInvitation(
                id = 0L,
                uuid = UUID.randomUUID(),
                equestrianCenterId = equestrianCenter.id,
                userId = invitedUser.id,
                invitedBy = requestingUserId,
                status = InvitationStatus.INVITED,
                invitedAt = now,
                respondedAt = null,
                expiresAt = expiresAt,
                createdAt = now,
                updatedAt = now,
                updatedBy = requestingUserId,
            )

        equestrianCenterInvitationRepository.save(equestrianCenterInvitation)
    }
}
