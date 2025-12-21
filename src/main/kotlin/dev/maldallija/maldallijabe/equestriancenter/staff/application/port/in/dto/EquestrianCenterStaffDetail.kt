package dev.maldallija.maldallijabe.equestriancenter.staff.application.port.`in`.dto

import java.time.Instant
import java.util.UUID

data class EquestrianCenterStaffDetail(
    val staffUuid: UUID,
    val userId: Long,
    val userUuid: UUID,
    val userNickname: String,
    val joinedAt: Instant,
)
