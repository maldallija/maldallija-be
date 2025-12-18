package dev.maldallija.maldallijabe.administration.adapter.`in`.web.equestriancenter.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.UUID

@Schema(description = "승마장 생성 요청")
data class CreateEquestrianCenterRequest(
    @field:NotBlank
    @field:Size(max = 128)
    @Schema(description = "승마장명", example = "서울승마클럽")
    val name: String,
    @Schema(description = "승마장 설명", example = "서울 강남구에 위치한 승마 클럽입니다")
    val description: String?,
    @field:NotNull
    @Schema(description = "대표 사용자 UUID")
    val representativeUserUuid: UUID,
)
