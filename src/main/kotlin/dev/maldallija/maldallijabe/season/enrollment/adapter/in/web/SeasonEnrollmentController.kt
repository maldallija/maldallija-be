package dev.maldallija.maldallijabe.season.enrollment.adapter.`in`.web

import dev.maldallija.maldallijabe.season.enrollment.application.port.`in`.ApplyToSeasonUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Season Enrollment", description = "승마장 시즌 참여 신청 API")
@RestController
@RequestMapping("/api/v1/equestrian-centers")
class SeasonEnrollmentController(
    private val applyToSeasonUseCase: ApplyToSeasonUseCase,
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
}
