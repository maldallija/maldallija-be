package dev.maldallija.maldallijabe.lesson.adapter.`in`.web

import dev.maldallija.maldallijabe.common.adapter.`in`.web.ErrorResponse
import dev.maldallija.maldallijabe.lesson.adapter.`in`.web.dto.CreateLessonRequest
import dev.maldallija.maldallijabe.lesson.adapter.`in`.web.dto.InstructorResponse
import dev.maldallija.maldallijabe.lesson.adapter.`in`.web.dto.LessonListResponse
import dev.maldallija.maldallijabe.lesson.adapter.`in`.web.dto.UpdateLessonRequest
import dev.maldallija.maldallijabe.lesson.application.port.`in`.CreateLessonUseCase
import dev.maldallija.maldallijabe.lesson.application.port.`in`.GetLessonDetailUseCase
import dev.maldallija.maldallijabe.lesson.application.port.`in`.GetLessonsUseCase
import dev.maldallija.maldallijabe.lesson.application.port.`in`.UpdateLessonUseCase
import dev.maldallija.maldallijabe.lesson.domain.LessonStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@Tag(name = "Lesson", description = "레슨 관리 API")
@RestController
@RequestMapping("/api/v1/equestrian-centers")
class LessonController(
    private val createLessonUseCase: CreateLessonUseCase,
    private val getLessonsUseCase: GetLessonsUseCase,
    private val getLessonDetailUseCase: GetLessonDetailUseCase,
    private val updateLessonUseCase: UpdateLessonUseCase,
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

    @Operation(summary = "레슨 목록 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장 또는 시즌을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @GetMapping("/{equestrianCenterUuid}/seasons/{seasonUuid}/lessons")
    fun getLessons(
        @PathVariable equestrianCenterUuid: UUID,
        @PathVariable seasonUuid: UUID,
        @RequestParam(required = false) lessonDate: LocalDate?,
        @RequestParam(required = false) lessonStatus: LessonStatus?,
    ): ResponseEntity<List<LessonListResponse>> {
        val lessons =
            getLessonsUseCase.getLessons(
                equestrianCenterUuid = equestrianCenterUuid,
                seasonUuid = seasonUuid,
                lessonDate = lessonDate,
                lessonStatus = lessonStatus,
            )

        val response =
            lessons.map { lesson ->
                LessonListResponse(
                    uuid = lesson.uuid,
                    title = lesson.title,
                    description = lesson.description,
                    lessonDate = lesson.lessonDate,
                    startTime = lesson.startTime,
                    endTime = lesson.endTime,
                    capacity = lesson.capacity,
                    currentCount = lesson.currentCount,
                    ridingCenter = lesson.ridingCenter,
                    status = lesson.status,
                    instructors =
                        lesson.instructors.map { instructor ->
                            InstructorResponse(
                                staffUuid = instructor.staffUuid,
                                name = instructor.name,
                            )
                        },
                )
            }

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "레슨 상세 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장, 시즌 또는 레슨을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @GetMapping("/{equestrianCenterUuid}/seasons/{seasonUuid}/lessons/{lessonUuid}")
    fun getLessonDetail(
        @PathVariable equestrianCenterUuid: UUID,
        @PathVariable seasonUuid: UUID,
        @PathVariable lessonUuid: UUID,
    ): ResponseEntity<LessonListResponse> {
        val lesson =
            getLessonDetailUseCase.getLessonDetail(
                equestrianCenterUuid = equestrianCenterUuid,
                seasonUuid = seasonUuid,
                lessonUuid = lessonUuid,
            )

        val response =
            LessonListResponse(
                uuid = lesson.uuid,
                title = lesson.title,
                description = lesson.description,
                lessonDate = lesson.lessonDate,
                startTime = lesson.startTime,
                endTime = lesson.endTime,
                capacity = lesson.capacity,
                currentCount = lesson.currentCount,
                ridingCenter = lesson.ridingCenter,
                status = lesson.status,
                instructors =
                    lesson.instructors.map { instructor ->
                        InstructorResponse(
                            staffUuid = instructor.staffUuid,
                            name = instructor.name,
                        )
                    },
            )

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "레슨 수정")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "수정 성공",
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (시간, 정원, 강사 등)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "해당 승마장의 직원만 레슨 수정 가능",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "승마장, 시즌 또는 레슨을 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    @PatchMapping("/{equestrianCenterUuid}/seasons/{seasonUuid}/lessons/{lessonUuid}")
    fun updateLesson(
        @PathVariable equestrianCenterUuid: UUID,
        @PathVariable seasonUuid: UUID,
        @PathVariable lessonUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
        @RequestBody request: UpdateLessonRequest,
    ): ResponseEntity<Void> {
        updateLessonUseCase.updateLesson(
            equestrianCenterUuid = equestrianCenterUuid,
            seasonUuid = seasonUuid,
            lessonUuid = lessonUuid,
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

        return ResponseEntity.ok().build()
    }
}
