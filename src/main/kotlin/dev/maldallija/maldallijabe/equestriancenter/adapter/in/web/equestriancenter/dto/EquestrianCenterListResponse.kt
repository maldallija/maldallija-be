package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.equestriancenter.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "승마장 목록 응답")
data class EquestrianCenterListResponse(
    @Schema(description = "승마장 UUID")
    val uuid: UUID,
    @Schema(description = "승마장명")
    val name: String,
    @Schema(description = "승마장 설명")
    val description: String?,
)
