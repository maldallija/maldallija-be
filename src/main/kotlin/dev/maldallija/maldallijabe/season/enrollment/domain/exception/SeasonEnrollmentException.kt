package dev.maldallija.maldallijabe.season.enrollment.domain.exception

import dev.maldallija.maldallijabe.common.domain.exception.BaseException

sealed class SeasonEnrollmentException(
    errorCode: String,
    message: String,
) : BaseException(errorCode, message)

class CannotApplyToInactiveSeasonException :
    SeasonEnrollmentException(
        errorCode = "CANNOT_APPLY_TO_INACTIVE_SEASON",
        message = "활성화된 시즌에만 참여 신청할 수 있습니다.",
    )

class DuplicateEnrollmentException :
    SeasonEnrollmentException(
        errorCode = "DUPLICATE_ENROLLMENT",
        message = "이미 참여 신청 또는 승인된 상태입니다.",
    )

class SeasonCapacityExceededException :
    SeasonEnrollmentException(
        errorCode = "SEASON_CAPACITY_EXCEEDED",
        message = "시즌 정원이 초과되었습니다.",
    )

class SeasonEnrollmentNotFoundException :
    SeasonEnrollmentException(
        errorCode = "SEASON_ENROLLMENT_NOT_FOUND",
        message = "시즌 참여 신청을 찾을 수 없습니다.",
    )

class UnauthorizedSeasonEnrollmentOperationException :
    SeasonEnrollmentException(
        errorCode = "UNAUTHORIZED_SEASON_ENROLLMENT_OPERATION",
        message = "시즌 참여 신청 작업 권한이 없습니다.",
    )

class InvalidEnrollmentStatusException :
    SeasonEnrollmentException(
        errorCode = "INVALID_ENROLLMENT_STATUS",
        message = "참여 신청 상태가 올바르지 않습니다.",
    )
