package dev.maldallija.maldallijabe.season.adapter.`in`.web

import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import dev.maldallija.maldallijabe.season.adapter.`in`.web.dto.CreateSeasonRequest
import dev.maldallija.maldallijabe.season.application.port.`in`.CreateSeasonUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Season", description = "시즌 관리 API")
@RestController
@RequestMapping("/api/v1/equestrian-centers")
class SeasonController(
    private val createSeasonUseCase: CreateSeasonUseCase,
) {
    @Operation(summary = "시즌 생성")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "생성 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (날짜 범위, 정원, 티켓 수 등)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "해당 승마장의 직원만 시즌 생성 가능",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PostMapping("/{equestrianCenterUuid}/seasons")
    fun createSeason(
        @PathVariable equestrianCenterUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
        @RequestBody request: CreateSeasonRequest,
    ): ResponseEntity<Void> {
        createSeasonUseCase.createSeason(
            equestrianCenterUuid = equestrianCenterUuid,
            requestingUserId = requestingUserId,
            title = request.title,
            description = request.description,
            startDate = request.startDate,
            endDate = request.endDate,
            capacity = request.capacity,
            defaultTicketCount = request.defaultTicketCount,
        )

        return ResponseEntity.status(201).build()
    }
}
