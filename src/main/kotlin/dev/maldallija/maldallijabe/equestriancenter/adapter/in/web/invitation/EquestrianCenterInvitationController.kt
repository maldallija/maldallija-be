package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.invitation

import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.invitation.dto.CreateEquestrianCenterInvitationRequest
import dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.invitation.dto.EquestrianCenterInvitationListResponse
import dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.invitation.dto.InvitedUserResponse
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.CreateEquestrianCenterInvitationUseCase
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.GetEquestrianCenterInvitationsUseCase
import dev.maldallija.maldallijabe.equestriancenter.invitation.application.port.`in`.WithdrawEquestrianCenterInvitationUseCase
import dev.maldallija.maldallijabe.equestriancenter.invitation.domain.InvitationStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "EquestrianCenter - Invitation", description = "승마장 직원 초대 API")
@RestController
@RequestMapping("/api/v1/equestrian-centers")
class EquestrianCenterInvitationController(
    private val createEquestrianCenterInvitationUseCase: CreateEquestrianCenterInvitationUseCase,
    private val getEquestrianCenterInvitationsUseCase: GetEquestrianCenterInvitationsUseCase,
    private val withdrawEquestrianCenterInvitationUseCase: WithdrawEquestrianCenterInvitationUseCase,
) {
    @Operation(summary = "발송한 승마장 직원 초대 목록 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
            ApiResponse(
                responseCode = "403",
                description = "대표 사용자만 조회 가능",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @GetMapping("/{equestrianCenterUuid}/invitations")
    fun getEquestrianCenterInvitations(
        @PathVariable equestrianCenterUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
        @RequestParam(required = false) status: InvitationStatus?,
        @PageableDefault(size = 20, sort = ["invitedAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ResponseEntity<Page<EquestrianCenterInvitationListResponse>> {
        val invitations =
            getEquestrianCenterInvitationsUseCase.getEquestrianCenterInvitations(
                equestrianCenterUuid = equestrianCenterUuid,
                requestingUserId = requestingUserId,
                status = status,
                pageable = pageable,
            )

        val response =
            invitations.map { invitation ->
                EquestrianCenterInvitationListResponse(
                    invitationUuid = invitation.invitationUuid,
                    invitedUser =
                        InvitedUserResponse(
                            uuid = invitation.invitedUserUuid,
                            nickname = invitation.invitedUserNickname,
                        ),
                    status = invitation.status,
                    invitedAt = invitation.invitedAt,
                    expiresAt = invitation.expiresAt,
                    respondedAt = invitation.respondedAt,
                )
            }

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "승마장 직원 초대")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "초대 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "이미 초대 발송됨 / 이미 직원임 / 자기 자신 초대 불가",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "대표 사용자만 초대 가능",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장을 찾을 수 없음 / 사용자를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PostMapping("/{equestrianCenterUuid}/invitations")
    fun createEquestrianCenterInvitation(
        @PathVariable equestrianCenterUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
        @Valid @RequestBody request: CreateEquestrianCenterInvitationRequest,
    ): ResponseEntity<Unit> {
        createEquestrianCenterInvitationUseCase.createEquestrianCenterInvitation(
            equestrianCenterUuid = equestrianCenterUuid,
            requestingUserId = requestingUserId,
            invitedUserUuid = request.userUuid,
        )

        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @Operation(summary = "승마장 직원 초대 취소")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "취소 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "INVITED 상태만 취소 가능",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "대표 사용자만 취소 가능",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장을 찾을 수 없음 / 초대를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @DeleteMapping("/{equestrianCenterUuid}/invitations/{invitationUuid}")
    fun withdrawEquestrianCenterInvitation(
        @PathVariable equestrianCenterUuid: UUID,
        @PathVariable invitationUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
    ): ResponseEntity<Unit> {
        withdrawEquestrianCenterInvitationUseCase.withdrawEquestrianCenterInvitation(
            equestrianCenterUuid = equestrianCenterUuid,
            invitationUuid = invitationUuid,
            requestingUserId = requestingUserId,
        )

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
