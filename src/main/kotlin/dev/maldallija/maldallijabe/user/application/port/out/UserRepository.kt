package dev.maldallija.maldallijabe.user.application.port.out

import dev.maldallija.maldallijabe.user.domain.User

interface UserRepository {
    fun existsByUsername(username: String): Boolean

    fun findByUsername(username: String): User?

    fun save(user: User): User
}
