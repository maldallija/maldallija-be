package dev.maldallija.maldallijabe.user.adapter.out.persistence

import dev.maldallija.maldallijabe.user.adapter.out.persistence.mapper.UserMapper
import dev.maldallija.maldallijabe.user.adapter.out.persistence.repository.UserJpaRepository
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import dev.maldallija.maldallijabe.user.domain.User
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository,
    private val userMapper: UserMapper,
) : UserRepository {
    override fun existsByUsername(username: String): Boolean = userJpaRepository.existsByUsername(username)

    override fun findById(id: Long): User? =
        userJpaRepository.findById(id).orElse(null)?.let {
            userMapper.toDomain(it)
        }

    override fun findAllByIdIn(ids: List<Long>): List<User> = userJpaRepository.findAllByIdIn(ids).map { userMapper.toDomain(it) }

    override fun findByUuid(uuid: UUID): User? =
        userJpaRepository.findByUuid(uuid)?.let {
            userMapper.toDomain(it)
        }

    override fun findByUsername(username: String): User? = userJpaRepository.findByUsername(username)?.let { userMapper.toDomain(it) }

    override fun save(user: User): User {
        val entity = userMapper.toEntity(user)
        val savedEntity = userJpaRepository.save(entity)
        return userMapper.toDomain(savedEntity)
    }
}
