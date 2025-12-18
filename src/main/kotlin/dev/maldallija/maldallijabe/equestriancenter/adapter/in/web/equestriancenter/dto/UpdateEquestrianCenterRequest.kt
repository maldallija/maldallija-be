package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.equestriancenter.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "승마장 정보 수정 요청")
data class UpdateEquestrianCenterRequest(
    @field:Size(max = 128)
    @Schema(description = "승마장명")
    val name: String?,
    @Schema(description = "승마장 설명")
    val description: String?,
)
