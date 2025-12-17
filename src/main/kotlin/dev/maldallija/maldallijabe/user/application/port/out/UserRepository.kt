package dev.maldallija.maldallijabe.user.application.port.out

import dev.maldallija.maldallijabe.user.domain.User
import java.util.UUID

interface UserRepository {
    fun existsByUsername(username: String): Boolean

    fun findById(id: Long): User?

    fun findByUuid(uuid: UUID): User?

    fun findByUsername(username: String): User?

    fun save(user: User): User
}
