package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.invitation

import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.invitation.dto.InvitingEquestrianCenterResponse
import dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.invitation.dto.UserEquestrianCenterInvitationListResponse
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.GetUserEquestrianCenterInvitationsUseCase
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "User - EquestrianCenter Invitation", description = "사용자 승마장 직원 초대 API")
@RestController
@RequestMapping("/api/v1/users")
class UserEquestrianCenterInvitationController(
    private val getUserEquestrianCenterInvitationsUseCase: GetUserEquestrianCenterInvitationsUseCase,
) {
    @Operation(summary = "사용자가 받은 승마장 직원 초대 목록 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
            ApiResponse(
                responseCode = "403",
                description = "본인만 조회 가능",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음 / 승마장을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @GetMapping("/{userUuid}/equestrian-center-invitations")
    fun getUserEquestrianCenterInvitations(
        @PathVariable userUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
        @RequestParam(required = false) status: InvitationStatus?,
        @PageableDefault(size = 20, sort = ["invitedAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ResponseEntity<Page<UserEquestrianCenterInvitationListResponse>> {
        val invitations =
            getUserEquestrianCenterInvitationsUseCase.getUserEquestrianCenterInvitations(
                userUuid = userUuid,
                requestingUserId = requestingUserId,
                status = status,
                pageable = pageable,
            )

        val response =
            invitations.map { invitation ->
                UserEquestrianCenterInvitationListResponse(
                    invitationUuid = invitation.invitationUuid,
                    equestrianCenter =
                        InvitingEquestrianCenterResponse(
                            uuid = invitation.equestrianCenterUuid,
                            name = invitation.equestrianCenterName,
                        ),
                    status = invitation.status,
                    invitedAt = invitation.invitedAt,
                    expiresAt = invitation.expiresAt,
                    respondedAt = invitation.respondedAt,
                )
            }

        return ResponseEntity.ok(response)
    }
}
