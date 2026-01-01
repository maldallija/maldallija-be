package dev.maldallija.maldallijabe.season.enrollment.adapter.`in`.web.dto

import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "시즌 참여 신청 목록 응답")
data class SeasonEnrollmentListResponse(
    @Schema(description = "신청 UUID")
    val enrollmentUuid: UUID,
    @Schema(description = "신청자 정보")
    val member: MemberResponse,
    @Schema(description = "신청 상태")
    val enrollmentStatus: EnrollmentStatus,
    @Schema(description = "신청 시간")
    val createdAt: Instant,
)
