package dev.maldallija.maldallijabe.season.domain.exception

import dev.maldallija.maldallijabe.common.domain.exception.BaseException

sealed class SeasonException(
    errorCode: String,
    message: String,
) : BaseException(errorCode, message)

class SeasonNotFoundException :
    SeasonException(
        errorCode = "SEASON_NOT_FOUND",
        message = "시즌을 찾을 수 없습니다.",
    )

class InvalidSeasonDateRangeException :
    SeasonException(
        errorCode = "INVALID_SEASON_DATE_RANGE",
        message = "시즌 종료일은 시작일보다 이후여야 합니다.",
    )

class InvalidSearchDateRangeException(
    message: String,
) : SeasonException(
        errorCode = "INVALID_SEARCH_DATE_RANGE",
        message = message,
    )

class InvalidSeasonCapacityException :
    SeasonException(
        errorCode = "INVALID_SEASON_CAPACITY",
        message = "시즌 정원은 1명 이상이어야 합니다.",
    )

class InvalidDefaultTicketCountException :
    SeasonException(
        errorCode = "INVALID_DEFAULT_TICKET_COUNT",
        message = "기본 티켓 수는 1개 이상이어야 합니다.",
    )

class UnauthorizedSeasonOperationException :
    SeasonException(
        errorCode = "UNAUTHORIZED_SEASON_OPERATION",
        message = "해당 승마장의 직원만 시즌을 관리할 수 있습니다.",
    )

class CannotUpdateClosedSeasonException :
    SeasonException(
        errorCode = "CANNOT_UPDATE_CLOSED_SEASON",
        message = "종료된 시즌은 수정할 수 없습니다.",
    )

class InvalidSeasonTitleException :
    SeasonException(
        errorCode = "INVALID_SEASON_TITLE",
        message = "시즌명은 공백일 수 없습니다.",
    )

class CannotCloseInactiveSeasonException :
    SeasonException(
        errorCode = "CANNOT_CLOSE_INACTIVE_SEASON",
        message = "활성화된 시즌만 종료할 수 있습니다.",
    )

class LessonsExistOutsideDateRangeException :
    SeasonException(
        errorCode = "LESSONS_EXIST_OUTSIDE_DATE_RANGE",
        message = "새로운 시즌 기간 밖에 존재하는 레슨이 있어 날짜를 변경할 수 없습니다.",
    )
