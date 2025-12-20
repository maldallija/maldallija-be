package dev.maldallija.maldallijabe.user.adapter.out.persistence.repository

import dev.maldallija.maldallijabe.user.adapter.out.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun existsByUsername(username: String): Boolean

    fun findAllByIdIn(ids: List<Long>): List<UserEntity>

    fun findByUuid(uuid: UUID): UserEntity?

    fun findByUsername(username: String): UserEntity?
}
