package dev.maldallija.maldallijabe.season.adapter.`in`.web.dto

import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class SeasonListResponse(
    @Schema(description = "시즌 UUID")
    val seasonUuid: UUID,
    @Schema(description = "시즌명", example = "2024 봄 시즌")
    val title: String,
    @Schema(description = "시작일", example = "2024-03-01")
    val startDate: LocalDate,
    @Schema(description = "종료일", example = "2024-05-31")
    val endDate: LocalDate,
    @Schema(description = "정원", example = "20")
    val capacity: Int,
    @Schema(description = "시즌 상태", example = "ACTIVE")
    val status: SeasonStatus,
)
