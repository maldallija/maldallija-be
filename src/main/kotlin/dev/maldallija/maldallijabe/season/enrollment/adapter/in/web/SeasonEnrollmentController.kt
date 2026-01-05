package dev.maldallija.maldallijabe.season.enrollment.adapter.`in`.web

import dev.maldallija.maldallijabe.season.enrollment.adapter.`in`.web.dto.MemberResponse
import dev.maldallija.maldallijabe.season.enrollment.adapter.`in`.web.dto.SeasonEnrollmentListResponse
import dev.maldallija.maldallijabe.season.enrollment.application.port.`in`.ApplyToSeasonUseCase
import dev.maldallija.maldallijabe.season.enrollment.application.port.`in`.ApproveSeasonEnrollmentUseCase
import dev.maldallija.maldallijabe.season.enrollment.application.port.`in`.GetSeasonEnrollmentsUseCase
import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Season Enrollment", description = "승마장 시즌 참여 신청 API")
@RestController
@RequestMapping("/api/v1/equestrian-centers")
class SeasonEnrollmentController(
    private val applyToSeasonUseCase: ApplyToSeasonUseCase,
    private val getSeasonEnrollmentsUseCase: GetSeasonEnrollmentsUseCase,
    private val approveSeasonEnrollmentUseCase: ApproveSeasonEnrollmentUseCase,
) {
    @Operation(summary = "승마장 시즌 참여 신청", description = "시즌에 참여 신청합니다.")
    @PostMapping("/{equestrianCenterUuid}/seasons/{seasonUuid}/enrollments")
    fun applyToSeason(
        @PathVariable equestrianCenterUuid: UUID,
        @PathVariable seasonUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
    ): ResponseEntity<Void> {
        applyToSeasonUseCase.applyToSeason(
            equestrianCenterUuid = equestrianCenterUuid,
            seasonUuid = seasonUuid,
            requestingUserId = requestingUserId,
        )
        return ResponseEntity.status(201).build()
    }

    @Operation(summary = "승마장 시즌 참여 신청 목록 조회", description = "시즌의 참여 신청 목록을 조회합니다 (직원용)")
    @GetMapping("/{equestrianCenterUuid}/seasons/{seasonUuid}/enrollments")
    fun getSeasonEnrollments(
        @PathVariable equestrianCenterUuid: UUID,
        @PathVariable seasonUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
        @RequestParam(required = false) enrollmentStatus: EnrollmentStatus?,
        @PageableDefault(size = 20) pageable: Pageable,
    ): ResponseEntity<Page<SeasonEnrollmentListResponse>> {
        val enrollments =
            getSeasonEnrollmentsUseCase.getSeasonEnrollments(
                equestrianCenterUuid = equestrianCenterUuid,
                seasonUuid = seasonUuid,
                requestingUserId = requestingUserId,
                enrollmentStatus = enrollmentStatus,
                pageable = pageable,
            )

        val response =
            enrollments.map { enrollment ->
                SeasonEnrollmentListResponse(
                    enrollmentUuid = enrollment.enrollmentUuid,
                    member =
                        MemberResponse(
                            memberUuid = enrollment.memberUuid,
                            memberNickname = enrollment.memberNickname,
                        ),
                    enrollmentStatus = enrollment.enrollmentStatus,
                    createdAt = enrollment.createdAt,
                )
            }

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "승마장 시즌 참여 신청 승인", description = "시즌 참여 신청을 승인합니다 (직원용)")
    @PostMapping("/{equestrianCenterUuid}/seasons/{seasonUuid}/enrollments/{enrollmentUuid}/approve")
    fun approveSeasonEnrollment(
        @PathVariable equestrianCenterUuid: UUID,
        @PathVariable seasonUuid: UUID,
        @PathVariable enrollmentUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
    ): ResponseEntity<Void> {
        approveSeasonEnrollmentUseCase.approveSeasonEnrollment(
            equestrianCenterUuid = equestrianCenterUuid,
            seasonUuid = seasonUuid,
            enrollmentUuid = enrollmentUuid,
            requestingUserId = requestingUserId,
        )
        return ResponseEntity.noContent().build()
    }
}
