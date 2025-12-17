package dev.maldallija.maldallijabe.administration.adapter.`in`.web.equestriancenter

import dev.maldallija.maldallijabe.administration.adapter.`in`.web.equestriancenter.dto.CreateEquestrianCenterRequest
import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import dev.maldallija.maldallijabe.equestriancenter.application.port.`in`.CreateEquestrianCenterUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Administration - EquestrianCenter", description = "관리자 - 승마장 관리 API")
@RestController
@RequestMapping("/api/v1/administration/equestrian-centers")
class EquestrianCenterAdministrationController(
    private val createEquestrianCenterUseCase: CreateEquestrianCenterUseCase,
) {
    @Operation(summary = "승마장 생성")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "승마장 생성 성공",
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "시스템 관리자 권한 필요",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "센터장 사용자를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PostMapping
    fun createEquestrianCenter(
        @AuthenticationPrincipal requestingUserId: Long,
        @RequestBody request: CreateEquestrianCenterRequest,
    ): ResponseEntity<Unit> {
        createEquestrianCenterUseCase.createEquestrianCenter(
            name = request.name,
            description = request.description,
            leaderUserUuid = request.leaderUserUuid,
            requestingUserId = requestingUserId,
        )

        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}
