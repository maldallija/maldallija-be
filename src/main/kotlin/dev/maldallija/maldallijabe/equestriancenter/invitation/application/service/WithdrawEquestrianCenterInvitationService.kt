package dev.maldallija.maldallijabe.equestriancenter.invitation.application.service

import dev.maldallija.maldallijabe.equestriancenter.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.UnauthorizedEquestrianCenterOperationException
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.WithdrawEquestrianCenterInvitationUseCase
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.out.EquestrianCenterInvitationRepository
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception.InvalidInvitationStatusException
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.exception.InvitationNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class WithdrawEquestrianCenterInvitationService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val equestrianCenterInvitationRepository: EquestrianCenterInvitationRepository,
) : WithdrawEquestrianCenterInvitationUseCase {
    override fun withdrawEquestrianCenterInvitation(
        equestrianCenterUuid: UUID,
        invitationUuid: UUID,
        requestingUserId: Long,
    ) {
        // 1. 센터 존재 & 삭제 안됨 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 2. 대표 권한 확인
        if (equestrianCenter.representativeUserId != requestingUserId) {
            throw UnauthorizedEquestrianCenterOperationException()
        }

        // 3. 초대 존재 확인
        val invitation =
            equestrianCenterInvitationRepository.findByUuid(invitationUuid)
                ?: throw InvitationNotFoundException()

        // 4. 초대가 해당 센터의 것인지 확인
        if (invitation.equestrianCenterId != equestrianCenter.id) {
            throw InvitationNotFoundException()
        }

        // 5. INVITED 상태인지 확인
        if (invitation.status != InvitationStatus.INVITED) {
            throw InvalidInvitationStatusException()
        }

        // 6. WITHDRAWN 상태로 변경
        val now = Instant.now()
        val withdrawnInvitation =
            invitation.copy(
                status = InvitationStatus.WITHDRAWN,
                updatedAt = now,
                updatedBy = requestingUserId,
            )

        equestrianCenterInvitationRepository.save(withdrawnInvitation)
    }
}
