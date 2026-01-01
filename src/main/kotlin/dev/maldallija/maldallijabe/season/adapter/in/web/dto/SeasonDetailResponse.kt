package dev.maldallija.maldallijabe.season.adapter.`in`.web.dto

import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class SeasonDetailResponse(
    @Schema(description = "시즌 UUID")
    val seasonUuid: UUID,
    @Schema(description = "승마장 정보")
    val equestrianCenter: EquestrianCenterInfo,
    @Schema(description = "시즌명", example = "2024 봄 시즌")
    val title: String,
    @Schema(description = "시즌 설명", example = "봄 시즌입니다")
    val description: String?,
    @Schema(description = "시작일", example = "2024-03-01")
    val startDate: LocalDate,
    @Schema(description = "종료일", example = "2024-05-31")
    val endDate: LocalDate,
    @Schema(description = "정원", example = "20")
    val capacity: Int,
    @Schema(description = "기본 티켓 수", example = "10")
    val defaultTicketCount: Int,
    @Schema(description = "시즌 상태", example = "ACTIVE")
    val status: SeasonStatus,
    @Schema(description = "생성 일시")
    val createdAt: Instant,
    @Schema(description = "수정 일시")
    val updatedAt: Instant,
)

data class EquestrianCenterInfo(
    @Schema(description = "승마장 UUID")
    val uuid: UUID,
    @Schema(description = "승마장 이름", example = "서울 승마클럽")
    val name: String,
)
