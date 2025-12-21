package dev.maldallija.maldallijabe.equestriancenter.staff.adapter.`in`.web

import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import dev.maldallija.maldallijabe.equestriancenter.staff.adapter.`in`.web.dto.EquestrianCenterStaffListResponse
import dev.maldallija.maldallijabe.equestriancenter.staff.adapter.`in`.web.dto.StaffUserResponse
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.GetEquestrianCenterStaffUseCase
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "EquestrianCenter - Staff", description = "승마장 직원 관리 API")
@RestController
@RequestMapping("/api/v1/equestrian-centers")
class EquestrianCenterStaffController(
    private val getEquestrianCenterStaffUseCase: GetEquestrianCenterStaffUseCase,
) {
    @Operation(summary = "승마장 직원 목록 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @GetMapping("/{equestrianCenterUuid}/staff")
    fun getEquestrianCenterStaff(
        @PathVariable equestrianCenterUuid: UUID,
        @PageableDefault(size = 20, sort = ["joinedAt"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ResponseEntity<Page<EquestrianCenterStaffListResponse>> {
        val staffPage =
            getEquestrianCenterStaffUseCase.getEquestrianCenterStaff(
                equestrianCenterUuid = equestrianCenterUuid,
                pageable = pageable,
            )

        val response =
            staffPage.map { staff ->
                EquestrianCenterStaffListResponse(
                    staffUuid = staff.staffUuid,
                    user =
                        StaffUserResponse(
                            uuid = staff.userUuid,
                            nickname = staff.userNickname,
                        ),
                    joinedAt = staff.joinedAt,
                )
            }

        return ResponseEntity.ok(response)
    }
}
