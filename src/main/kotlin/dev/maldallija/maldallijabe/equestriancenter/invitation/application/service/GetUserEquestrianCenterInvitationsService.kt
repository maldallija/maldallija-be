package dev.maldallija.maldallijabe.equestriancenter.invitation.application.service

import dev.maldallija.maldallijabe.equestriancenter.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.GetUserEquestrianCenterInvitationsUseCase
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.dto.UserEquestrianCenterInvitationDetail
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.out.EquestrianCenterInvitationRepository
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import dev.maldallija.maldallijabe.user.domain.exception.UnauthorizedUserOperationException
import dev.maldallija.maldallijabe.user.domain.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetUserEquestrianCenterInvitationsService(
    private val equestrianCenterInvitationRepository: EquestrianCenterInvitationRepository,
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val userRepository: UserRepository,
) : GetUserEquestrianCenterInvitationsUseCase {
    private val logger = LoggerFactory.getLogger(GetUserEquestrianCenterInvitationsService::class.java)

    override fun getUserEquestrianCenterInvitations(
        userUuid: UUID,
        requestingUserId: Long,
        status: InvitationStatus?,
        pageable: Pageable,
    ): Page<UserEquestrianCenterInvitationDetail> {
        // 1. 대상 사용자 조회
        val user = userRepository.findByUuid(userUuid) ?: throw UserNotFoundException()

        // 2. 권한 검증: 본인만 조회 가능
        if (user.id != requestingUserId) {
            throw UnauthorizedUserOperationException()
        }

        // 3. 받은 초대 목록 조회
        val invitations =
            equestrianCenterInvitationRepository.findByUserIdAndOptionalStatus(
                userId = user.id,
                status = status,
                pageable = pageable,
            )

        // 4. 승마장 정보 한 번에 조회 (N+1 방지)
        val equestrianCenterIds = invitations.content.map { it.equestrianCenterId }
        val equestrianCenters =
            if (equestrianCenterIds.isEmpty()) {
                emptyMap()
            } else {
                equestrianCenterRepository
                    .findAllByIdIn(equestrianCenterIds)
                    .associateBy { it.id }
            }

        // 5. 초대 정보와 승마장 정보 매핑
        return invitations.map { invitation ->
            val equestrianCenter = equestrianCenters[invitation.equestrianCenterId]

            if (equestrianCenter == null) {
                logger.warn(
                    "EquestrianCenter not found for invitation. " +
                        "invitationUuid={}, equestrianCenterId={}, userId={}",
                    invitation.uuid,
                    invitation.equestrianCenterId,
                    invitation.userId,
                )
                UserEquestrianCenterInvitationDetail(
                    invitationUuid = invitation.uuid,
                    equestrianCenterUuid = UUID(0, 0),
                    equestrianCenterName = "알 수 없는 승마장",
                    invitationStatus = invitation.status,
                    invitedAt = invitation.invitedAt,
                    expiresAt = invitation.expiresAt,
                    respondedAt = invitation.respondedAt,
                )
            } else {
                UserEquestrianCenterInvitationDetail(
                    invitationUuid = invitation.uuid,
                    equestrianCenterUuid = equestrianCenter.uuid,
                    equestrianCenterName = equestrianCenter.name,
                    invitationStatus = invitation.status,
                    invitedAt = invitation.invitedAt,
                    expiresAt = invitation.expiresAt,
                    respondedAt = invitation.respondedAt,
                )
            }
        }
    }
}
