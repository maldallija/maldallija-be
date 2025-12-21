package dev.maldallija.maldallijabe.equestriancenter.staff.domain.exception

class UnauthorizedStaffOperationException :
    EquestrianCenterStaffException(
        errorCode = "UNAUTHORIZED_STAFF_OPERATION",
        message = "Unauthorized staff operation",
    )
