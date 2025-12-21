package dev.maldallija.maldallijabe.equestriancenter.staff.domain

import java.time.Instant
import java.util.UUID

data class EquestrianCenterStaff(
    val id: Long,
    val uuid: UUID,
    val equestrianCenterId: Long,
    val userId: Long,
    val joinedAt: Instant,
    val leftAt: Instant?,
    val leftBy: Long?,
    val leftReason: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val updatedBy: Long,
)
