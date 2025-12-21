package dev.maldallija.maldallijabe.equestriancenter.staff.adapter.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class StaffEquestrianCenterResponse(
    @Schema(description = "승마장 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    val uuid: UUID,
    @Schema(description = "승마장 이름", example = "서울승마클럽")
    val name: String,
)
