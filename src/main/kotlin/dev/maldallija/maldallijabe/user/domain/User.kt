package dev.maldallija.maldallijabe.user.domain

import java.time.Instant
import java.util.UUID

data class User(
    val id: Long,
    val uuid: UUID,
    val username: String,
    val password: String,
    val nickname: String,
    val isSystemAdmin: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deletedAt: Instant?,
) {
    fun isDeleted(): Boolean = deletedAt != null
}
