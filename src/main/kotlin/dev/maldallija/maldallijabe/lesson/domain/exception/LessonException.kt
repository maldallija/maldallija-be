package dev.maldallija.maldallijabe.lesson.domain.exception

import dev.maldallija.maldallijabe.common.domain.exception.BaseException

sealed class LessonException(
    errorCode: String,
    message: String,
) : BaseException(errorCode, message)

class LessonNotFoundException :
    LessonException(
        errorCode = "LESSON_NOT_FOUND",
        message = "레슨을 찾을 수 없습니다.",
    )

class InvalidLessonTimeException :
    LessonException(
        errorCode = "INVALID_LESSON_TIME",
        message = "레슨 종료 시간은 시작 시간보다 이후여야 합니다.",
    )

class InvalidLessonCapacityException :
    LessonException(
        errorCode = "INVALID_LESSON_CAPACITY",
        message = "레슨 정원은 1명 이상이어야 합니다.",
    )

class InvalidLessonTitleException :
    LessonException(
        errorCode = "INVALID_LESSON_TITLE",
        message = "레슨명은 공백일 수 없습니다.",
    )

class LessonNotInSeasonPeriodException :
    LessonException(
        errorCode = "LESSON_NOT_IN_SEASON_PERIOD",
        message = "레슨 날짜는 시즌 기간 내에 있어야 합니다.",
    )

class SeasonNotActiveException :
    LessonException(
        errorCode = "SEASON_NOT_ACTIVE",
        message = "활성화된 시즌에서만 레슨을 생성/수정할 수 있습니다.",
    )

class AtLeastOneInstructorRequiredException :
    LessonException(
        errorCode = "AT_LEAST_ONE_INSTRUCTOR_REQUIRED",
        message = "레슨에는 최소 1명의 강사가 필요합니다.",
    )

class InstructorNotBelongToCenterException :
    LessonException(
        errorCode = "INSTRUCTOR_NOT_BELONG_TO_CENTER",
        message = "강사가 해당 승마장의 직원이 아닙니다.",
    )

class UnauthorizedLessonOperationException :
    LessonException(
        errorCode = "UNAUTHORIZED_LESSON_OPERATION",
        message = "해당 승마장의 직원만 레슨을 관리할 수 있습니다.",
    )

class DuplicateInstructorException :
    LessonException(
        errorCode = "DUPLICATE_INSTRUCTOR",
        message = "중복된 강사가 포함되어 있습니다.",
    )
