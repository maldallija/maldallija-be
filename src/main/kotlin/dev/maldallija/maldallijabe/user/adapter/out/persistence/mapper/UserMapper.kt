package dev.maldallija.maldallijabe.user.adapter.out.persistence.mapper

import dev.maldallija.maldallijabe.user.adapter.out.persistence.entity.UserEntity
import dev.maldallija.maldallijabe.user.domain.User
import org.springframework.stereotype.Component

@Component
class UserMapper {
    fun toDomain(entity: UserEntity): User =
        User(
            id = entity.id,
            uuid = entity.uuid,
            username = entity.username,
            password = entity.password,
            nickname = entity.nickname,
            isSystemAdmin = entity.isSystemAdmin,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            deletedAt = entity.deletedAt,
        )

    fun toEntity(domain: User): UserEntity =
        UserEntity(
            id = domain.id,
            uuid = domain.uuid,
            username = domain.username,
            password = domain.password,
            nickname = domain.nickname,
            isSystemAdmin = domain.isSystemAdmin,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            deletedAt = domain.deletedAt,
        )
}
