package dev.maldallija.maldallijabe.equestriancenter.staff.domain.exception

class StaffNotFoundException :
    EquestrianCenterStaffException(
        errorCode = "STAFF_NOT_FOUND",
        message = "Staff not found",
    )
