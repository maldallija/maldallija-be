package dev.maldallija.maldallijabe.equestriancenter.invitation.application.service

import dev.maldallija.maldallijabe.equestriancenter.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.UnauthorizedEquestrianCenterOperationException
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.GetEquestrianCenterInvitationsUseCase
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.dto.EquestrianCenterInvitationDetail
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.out.EquestrianCenterInvitationRepository
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetEquestrianCenterInvitationsService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val equestrianCenterInvitationRepository: EquestrianCenterInvitationRepository,
    private val userRepository: UserRepository,
) : GetEquestrianCenterInvitationsUseCase {
    private val logger = LoggerFactory.getLogger(GetEquestrianCenterInvitationsService::class.java)

    override fun getEquestrianCenterInvitations(
        equestrianCenterUuid: UUID,
        requestingUserId: Long,
        status: InvitationStatus?,
        pageable: Pageable,
    ): Page<EquestrianCenterInvitationDetail> {
        // 1. 센터 존재 & 삭제 안됨 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 2. 대표 권한 확인
        if (equestrianCenter.representativeUserId != requestingUserId) {
            throw UnauthorizedEquestrianCenterOperationException()
        }

        // 3. 초대 목록 조회
        val invitations =
            equestrianCenterInvitationRepository.findByEquestrianCenterIdAndOptionalStatus(
                equestrianCenterId = equestrianCenter.id,
                status = status,
                pageable = pageable,
            )

        // 4. 초대받은 사용자 정보 한 번에 조회 (N+1 방지)
        val userIds = invitations.content.map { it.userId }
        val users =
            if (userIds.isEmpty()) {
                emptyMap()
            } else {
                userRepository
                    .findAllByIdIn(userIds)
                    .associateBy { it.id }
            }

        // 5. 초대 정보와 사용자 정보 매핑
        return invitations.map { invitation ->
            val invitedUser = users[invitation.userId]

            if (invitedUser == null) {
                logger.warn(
                    "User not found for invitation. " +
                        "invitationUuid={}, userId={}, equestrianCenterId={}",
                    invitation.uuid,
                    invitation.userId,
                    invitation.equestrianCenterId,
                )
                EquestrianCenterInvitationDetail(
                    invitationUuid = invitation.uuid,
                    invitedUserUuid = UUID(0, 0),
                    invitedUserNickname = "알 수 없는 사용자",
                    invitationStatus = invitation.status,
                    invitedAt = invitation.invitedAt,
                    expiresAt = invitation.expiresAt,
                    respondedAt = invitation.respondedAt,
                )
            } else {
                EquestrianCenterInvitationDetail(
                    invitationUuid = invitation.uuid,
                    invitedUserUuid = invitedUser.uuid,
                    invitedUserNickname = invitedUser.nickname,
                    invitationStatus = invitation.status,
                    invitedAt = invitation.invitedAt,
                    expiresAt = invitation.expiresAt,
                    respondedAt = invitation.respondedAt,
                )
            }
        }
    }
}
