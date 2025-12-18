package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.equestriancenter.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "승마장 상세 응답")
data class EquestrianCenterDetailResponse(
    @Schema(description = "승마장 UUID")
    val uuid: UUID,
    @Schema(description = "승마장명")
    val name: String,
    @Schema(description = "승마장 설명")
    val description: String?,
    @Schema(description = "승마장 대표 사용자 UUID")
    val leaderUserUuid: UUID,
    @Schema(description = "생성일")
    val createdAt: Instant,
    @Schema(description = "수정일")
    val updatedAt: Instant,
)
