package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.equestriancenter

import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.equestriancenter.dto.EquestrianCenterDetailResponse
import dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.equestriancenter.dto.EquestrianCenterListResponse
import dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.equestriancenter.dto.UpdateEquestrianCenterRequest
import dev.maldallija.maldallijabe.equestriancenter.application.port.`in`.GetEquestrianCenterUseCase
import dev.maldallija.maldallijabe.equestriancenter.application.port.`in`.GetEquestrianCentersUseCase
import dev.maldallija.maldallijabe.equestriancenter.application.port.`in`.UpdateEquestrianCenterUseCase
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
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "EquestrianCenter", description = "승마장 API")
@RestController
@RequestMapping("/api/v1/equestrian-centers")
class EquestrianCenterController(
    private val getEquestrianCentersUseCase: GetEquestrianCentersUseCase,
    private val getEquestrianCenterUseCase: GetEquestrianCenterUseCase,
    private val updateEquestrianCenterUseCase: UpdateEquestrianCenterUseCase,
) {
    @Operation(summary = "승마장 목록 조회")
    @GetMapping
    fun getEquestrianCenters(
        @PageableDefault(size = 20, sort = ["name"], direction = Sort.Direction.ASC) pageable: Pageable,
    ): ResponseEntity<Page<EquestrianCenterListResponse>> {
        val equestrianCenters = getEquestrianCentersUseCase.getEquestrianCenters(pageable)

        val response =
            equestrianCenters.map { equestrianCenter ->
                EquestrianCenterListResponse(
                    uuid = equestrianCenter.uuid,
                    name = equestrianCenter.name,
                    description = equestrianCenter.description,
                )
            }

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "승마장 상세 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "승마장 조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @GetMapping("/{equestrianCenterUuid}")
    fun getEquestrianCenter(
        @PathVariable equestrianCenterUuid: UUID,
    ): ResponseEntity<EquestrianCenterDetailResponse> {
        val equestrianCenterDetail = getEquestrianCenterUseCase.getEquestrianCenter(equestrianCenterUuid)

        val response =
            EquestrianCenterDetailResponse(
                uuid = equestrianCenterDetail.uuid,
                name = equestrianCenterDetail.name,
                description = equestrianCenterDetail.description,
                representativeUserUuid = equestrianCenterDetail.representativeUserUuid,
                createdAt = equestrianCenterDetail.createdAt,
                updatedAt = equestrianCenterDetail.updatedAt,
            )

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "승마장 정보 수정")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "승마장 수정 성공",
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "대표 사용자만 수정 가능",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PatchMapping("/{equestrianCenterUuid}")
    fun updateEquestrianCenter(
        @AuthenticationPrincipal representativeUserId: Long,
        @PathVariable equestrianCenterUuid: UUID,
        @RequestBody request: UpdateEquestrianCenterRequest,
    ): ResponseEntity<Unit> {
        updateEquestrianCenterUseCase.updateEquestrianCenter(
            equestrianCenterUuid = equestrianCenterUuid,
            representativeUserId = representativeUserId,
            name = request.name,
            description = request.description,
        )

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
