package dev.maldallija.maldallijabe.equestriancenter.staff.domain.exception

class StaffAlreadyLeftException :
    EquestrianCenterStaffException(
        errorCode = "STAFF_ALREADY_LEFT",
        message = "Staff already left",
    )
