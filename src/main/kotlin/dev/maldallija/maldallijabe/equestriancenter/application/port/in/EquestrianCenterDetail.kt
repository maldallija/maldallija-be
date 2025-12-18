package dev.maldallija.maldallijabe.equestriancenter.application.port.`in`

import java.time.Instant
import java.util.UUID

data class EquestrianCenterDetail(
    val uuid: UUID,
    val name: String,
    val description: String?,
    val representativeUserUuid: UUID,
    val createdAt: Instant,
    val updatedAt: Instant,
)
