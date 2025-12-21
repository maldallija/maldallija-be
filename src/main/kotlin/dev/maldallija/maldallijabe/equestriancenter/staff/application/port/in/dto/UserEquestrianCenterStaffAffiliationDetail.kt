package dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.dto

import java.time.Instant
import java.util.UUID

data class UserEquestrianCenterStaffAffiliationDetail(
    val staffUuid: UUID,
    val equestrianCenterId: Long,
    val equestrianCenterUuid: UUID,
    val equestrianCenterName: String,
    val joinedAt: Instant,
)
