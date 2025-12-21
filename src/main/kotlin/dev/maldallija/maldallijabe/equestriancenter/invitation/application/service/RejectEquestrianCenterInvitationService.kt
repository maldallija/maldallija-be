package dev.maldallija.maldallijabe.equestriancenter.invitation.application.service

import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.RejectEquestrianCenterInvitationUseCase
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.out.EquestrianCenterInvitationRepository
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception.InvitationAlreadyRespondedException
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception.InvitationExpiredException
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception.InvitationNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception.UnauthorizedInvitationOperationException
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import dev.maldallija.maldallijabe.user.domain.exception.UserNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class RejectEquestrianCenterInvitationService(
    private val userRepository: UserRepository,
    private val equestrianCenterInvitationRepository: EquestrianCenterInvitationRepository,
) : RejectEquestrianCenterInvitationUseCase {
    override fun rejectEquestrianCenterInvitation(
        userUuid: UUID,
        invitationUuid: UUID,
        actorUserId: Long,
    ) {
        // 1. 사용자 존재 확인
        val user =
            userRepository.findByUuid(userUuid)
                ?: throw UserNotFoundException()

        // 2. 초대 존재 확인
        val invitation =
            equestrianCenterInvitationRepository.findByUuid(invitationUuid)
                ?: throw InvitationNotFoundException()

        // 3. 초대된 사용자 본인인지 확인
        if (invitation.userId != user.id) {
            throw UnauthorizedInvitationOperationException()
        }

        // 4. 초대가 INVITED 상태인지 확인
        if (invitation.status != InvitationStatus.INVITED) {
            throw InvitationAlreadyRespondedException()
        }

        // 5. 초대가 만료되지 않았는지 확인
        val now = Instant.now()
        if (invitation.expiresAt.isBefore(now)) {
            throw InvitationExpiredException()
        }

        // 6. 초대 상태를 REJECTED로 변경
        val rejectedInvitation =
            invitation.copy(
                status = InvitationStatus.REJECTED,
                respondedAt = now,
                updatedAt = now,
                updatedBy = actorUserId,
            )

        equestrianCenterInvitationRepository.save(rejectedInvitation)
    }
}
