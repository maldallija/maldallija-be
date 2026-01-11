package dev.maldallija.maldallijabe.lesson.adapter.`in`.web

import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import dev.maldallija.maldallijabe.lesson.adapter.`in`.web.dto.CreateLessonRequest
import dev.maldallija.maldallijabe.lesson.application.port.`in`.CreateLessonUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "Lesson", description = "레슨 관리 API")
@RestController
@RequestMapping("/api/v1/equestrian-centers")
class LessonController(
    private val createLessonUseCase: CreateLessonUseCase,
) {
    @Operation(summary = "레슨 생성")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "생성 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (시간, 정원, 강사 등)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "해당 승마장의 직원만 레슨 생성 가능",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장 또는 시즌을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PostMapping("/{equestrianCenterUuid}/seasons/{seasonUuid}/lessons")
    fun createLesson(
        @PathVariable equestrianCenterUuid: UUID,
        @PathVariable seasonUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
        @RequestBody request: CreateLessonRequest,
    ): ResponseEntity<Void> {
        createLessonUseCase.createLesson(
            equestrianCenterUuid = equestrianCenterUuid,
            seasonUuid = seasonUuid,
            requestingUserId = requestingUserId,
            title = request.title,
            description = request.description,
            lessonDate = request.lessonDate,
            startTime = request.startTime,
            endTime = request.endTime,
            capacity = request.capacity,
            ridingCenter = request.ridingCenter,
            instructorStaffUuids = request.instructorStaffUuids,
        )

        return ResponseEntity.status(201).build()
    }
}
