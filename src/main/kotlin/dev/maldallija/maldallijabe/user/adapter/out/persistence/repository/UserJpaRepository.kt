package dev.maldallija.maldallijabe.user.adapter.out.persistence.repository

import dev.maldallija.maldallijabe.user.adapter.out.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun existsByUsername(username: String): Boolean

    fun findByUsername(username: String): UserEntity?
}
