package dev.maldallija.maldallijabe.equestriancenter.adapter.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "승마장 생성 요청")
data class CreateEquestrianCenterRequest(
    @Schema(description = "승마장명", example = "서울승마클럽")
    val name: String,
    @Schema(description = "승마장 설명", example = "서울 강남구에 위치한 승마 클럽입니다")
    val description: String?,
    @Schema(description = "센터장 사용자 UUID")
    val leaderUserUuid: UUID,
)
