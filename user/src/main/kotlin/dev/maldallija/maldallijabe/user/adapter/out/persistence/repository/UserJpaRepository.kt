package dev.maldallija.maldallijabe.user.adapter.out.persistence.repository

import dev.maldallija.maldallijabe.user.adapter.out.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun existsByUsernameAndDeletedAtIsNull(username: String): Boolean

    fun findByIdAndDeletedAtIsNull(id: Long): UserEntity?

    fun findAllByIdInAndDeletedAtIsNull(ids: List<Long>): List<UserEntity>

    fun findByUuidAndDeletedAtIsNull(uuid: UUID): UserEntity?

    fun findByUsernameAndDeletedAtIsNull(username: String): UserEntity?
}
