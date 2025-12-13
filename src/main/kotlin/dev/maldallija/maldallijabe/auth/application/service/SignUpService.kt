package dev.maldallija.maldallijabe.auth.application.service

import dev.maldallija.maldallijabe.auth.application.port.`in`.SignUpUseCase
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import dev.maldallija.maldallijabe.user.domain.User
import dev.maldallija.maldallijabe.user.domain.exception.DuplicateUsernameException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class SignUpService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : SignUpUseCase {
    override fun signUp(
        username: String,
        password: String,
        nickname: String,
    ): User {
        if (userRepository.existsByUsername(username)) {
            throw DuplicateUsernameException()
        }

        val hashedPassword = passwordEncoder.encode(password)
        val now = Instant.now()

        val user =
            User(
                id = 0,
                uuid = UUID.randomUUID(),
                username = username,
                password = hashedPassword,
                nickname = nickname,
                isSystemAdmin = false,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
            )

        return userRepository.save(user)
    }
}
