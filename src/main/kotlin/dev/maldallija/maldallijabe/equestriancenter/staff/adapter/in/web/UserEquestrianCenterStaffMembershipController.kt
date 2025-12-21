package dev.maldallija.maldallijabe.equestriancenter.staff.adapter.`in`.web

import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import dev.maldallija.maldallijabe.equestriancenter.staff.adapter.`in`.web.dto.StaffEquestrianCenterResponse
import dev.maldallija.maldallijabe.equestriancenter.staff.adapter.`in`.web.dto.UserEquestrianCenterStaffMembershipListResponse
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.GetUserEquestrianCenterStaffMembershipsUseCase
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
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "User - EquestrianCenter Staff Membership", description = "사용자 승마장 직원 자격 API")
@RestController
@RequestMapping("/api/v1/users")
class UserEquestrianCenterStaffMembershipController(
    private val getUserEquestrianCenterStaffMembershipsUseCase: GetUserEquestrianCenterStaffMembershipsUseCase,
) {
    @Operation(summary = "사용자가 직원으로 속한 승마장 목록 조회")
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
                description = "사용자를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @GetMapping("/{userUuid}/equestrian-center-staff-memberships")
    fun getUserEquestrianCenterStaffMemberships(
        @PathVariable userUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
        @PageableDefault(size = 20, sort = ["joinedAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ResponseEntity<Page<UserEquestrianCenterStaffMembershipListResponse>> {
        val membershipsPage =
            getUserEquestrianCenterStaffMembershipsUseCase.getUserEquestrianCenterStaffMemberships(
                userUuid = userUuid,
                requestingUserId = requestingUserId,
                pageable = pageable,
            )

        val response =
            membershipsPage.map { membership ->
                UserEquestrianCenterStaffMembershipListResponse(
                    staffUuid = membership.staffUuid,
                    equestrianCenter =
                        StaffEquestrianCenterResponse(
                            uuid = membership.equestrianCenterUuid,
                            name = membership.equestrianCenterName,
                        ),
                    joinedAt = membership.joinedAt,
                )
            }

        return ResponseEntity.ok(response)
    }
}
