package dev.maldallija.maldallijabe.season.adapter.`in`.web

import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import dev.maldallija.maldallijabe.season.adapter.`in`.web.dto.CreateSeasonRequest
import dev.maldallija.maldallijabe.season.adapter.`in`.web.dto.EquestrianCenterInfo
import dev.maldallija.maldallijabe.season.adapter.`in`.web.dto.SeasonDetailResponse
import dev.maldallija.maldallijabe.season.adapter.`in`.web.dto.SeasonListResponse
import dev.maldallija.maldallijabe.season.adapter.`in`.web.dto.UpdateSeasonRequest
import dev.maldallija.maldallijabe.season.application.port.`in`.CloseSeasonUseCase
import dev.maldallija.maldallijabe.season.application.port.`in`.CreateSeasonUseCase
import dev.maldallija.maldallijabe.season.application.port.`in`.GetEquestrianCenterSeasonsUseCase
import dev.maldallija.maldallijabe.season.application.port.`in`.GetSeasonDetailUseCase
import dev.maldallija.maldallijabe.season.application.port.`in`.UpdateSeasonUseCase
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
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
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@Tag(name = "Season", description = "시즌 관리 API")
@RestController
@RequestMapping("/api/v1/equestrian-centers")
class SeasonController(
    private val createSeasonUseCase: CreateSeasonUseCase,
    private val getEquestrianCenterSeasonsUseCase: GetEquestrianCenterSeasonsUseCase,
    private val getSeasonDetailUseCase: GetSeasonDetailUseCase,
    private val updateSeasonUseCase: UpdateSeasonUseCase,
    private val closeSeasonUseCase: CloseSeasonUseCase,
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

    @Operation(
        summary = "승마장 시즌 목록 조회",
        description = "status: 전체(null)/ACTIVE/CLOSED, startDate+endDate: 날짜 범위 조회 (ex: 2024-03-01 ~ 2024-03-31)",
    )
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
    @GetMapping("/{equestrianCenterUuid}/seasons")
    fun getEquestrianCenterSeasons(
        @PathVariable equestrianCenterUuid: UUID,
        @RequestParam(required = false) status: SeasonStatus?,
        @RequestParam(required = false) startDate: LocalDate?,
        @RequestParam(required = false) endDate: LocalDate?,
        @PageableDefault(size = 20, sort = ["startDate"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): ResponseEntity<Page<SeasonListResponse>> {
        val seasons =
            getEquestrianCenterSeasonsUseCase.getEquestrianCenterSeasons(
                equestrianCenterUuid = equestrianCenterUuid,
                status = status,
                startDate = startDate,
                endDate = endDate,
                pageable = pageable,
            )

        val response =
            seasons.map { season ->
                SeasonListResponse(
                    seasonUuid = season.seasonUuid,
                    title = season.title,
                    startDate = season.startDate,
                    endDate = season.endDate,
                    capacity = season.capacity,
                    status = season.status,
                )
            }

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "승마장 시즌 상세 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "시즌을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @GetMapping("/{equestrianCenterUuid}/seasons/{seasonUuid}")
    fun getSeasonDetail(
        @PathVariable equestrianCenterUuid: UUID,
        @PathVariable seasonUuid: UUID,
    ): ResponseEntity<SeasonDetailResponse> {
        val season =
            getSeasonDetailUseCase.getSeasonDetail(
                equestrianCenterUuid = equestrianCenterUuid,
                seasonUuid = seasonUuid,
            )

        val response =
            SeasonDetailResponse(
                seasonUuid = season.seasonUuid,
                equestrianCenter =
                    EquestrianCenterInfo(
                        uuid = season.equestrianCenterUuid,
                        name = season.equestrianCenterName,
                    ),
                title = season.title,
                description = season.description,
                startDate = season.startDate,
                endDate = season.endDate,
                capacity = season.capacity,
                defaultTicketCount = season.defaultTicketCount,
                status = season.status,
                createdAt = season.createdAt,
                updatedAt = season.updatedAt,
            )

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "승마장 시즌 수정")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "수정 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (날짜 범위, 정원, 티켓 수 등)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "해당 승마장의 직원만 시즌 수정 가능",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장 또는 시즌을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PatchMapping("/{equestrianCenterUuid}/seasons/{seasonUuid}")
    fun updateSeason(
        @PathVariable equestrianCenterUuid: UUID,
        @PathVariable seasonUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
        @RequestBody request: UpdateSeasonRequest,
    ): ResponseEntity<Void> {
        updateSeasonUseCase.updateSeason(
            equestrianCenterUuid = equestrianCenterUuid,
            seasonUuid = seasonUuid,
            requestingUserId = requestingUserId,
            title = request.title,
            description = request.description,
            startDate = request.startDate,
            endDate = request.endDate,
            capacity = request.capacity,
            defaultTicketCount = request.defaultTicketCount,
        )

        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "승마장 시즌 종료")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "종료 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "활성화된 시즌이 아님",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "해당 승마장의 직원만 시즌 종료 가능",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장 또는 시즌을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PatchMapping("/{equestrianCenterUuid}/seasons/{seasonUuid}/close")
    fun closeSeason(
        @PathVariable equestrianCenterUuid: UUID,
        @PathVariable seasonUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
    ): ResponseEntity<Void> {
        closeSeasonUseCase.closeSeason(
            equestrianCenterUuid = equestrianCenterUuid,
            seasonUuid = seasonUuid,
            requestingUserId = requestingUserId,
        )

        return ResponseEntity.noContent().build()
    }
}
