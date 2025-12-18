package dev.maldallija.maldallijabe.equestriancenter.domain

import java.time.Instant
import java.util.UUID

data class EquestrianCenter(
    val id: Long,
    val uuid: UUID,
    val name: String,
    val description: String?,
    val representativeUserId: Long,
    val createdBy: Long,
    val createdAt: Instant,
    val updatedBy: Long,
    val updatedAt: Instant,
    val deletedAt: Instant?,
) {
    fun isDeleted(): Boolean = deletedAt != null
}
