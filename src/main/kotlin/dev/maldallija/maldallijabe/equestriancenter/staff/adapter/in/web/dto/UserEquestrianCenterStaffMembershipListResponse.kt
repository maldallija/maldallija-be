package dev.maldallija.maldallijabe.equestriancenter.staff.adapter.`in`.web.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

data class UserEquestrianCenterStaffMembershipListResponse(
    @Schema(description = "직원 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    val staffUuid: UUID,
    @Schema(description = "승마장 정보")
    val equestrianCenter: StaffEquestrianCenterResponse,
    @Schema(description = "합류일", example = "2025-01-01T00:00:00Z")
    val joinedAt: Instant,
)
