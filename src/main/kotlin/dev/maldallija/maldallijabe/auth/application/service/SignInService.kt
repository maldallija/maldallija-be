package dev.maldallija.maldallijabe.auth.application.service

import dev.maldallija.maldallijabe.auth.application.port.`in`.SignInUseCase
import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationAccessSessionRepository
import dev.maldallija.maldallijabe.auth.config.AuthProperties
import dev.maldallija.maldallijabe.auth.domain.AuthenticationAccessSession
import dev.maldallija.maldallijabe.auth.domain.exception.InvalidCredentialsException
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class SignInService(
    private val authProperties: AuthProperties,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val authenticationAccessSessionRepository: AuthenticationAccessSessionRepository,
) : SignInUseCase {
    override fun signIn(
        username: String,
        password: String,
    ): AuthenticationAccessSession {
        val user =
            userRepository.findByUsername(username)
                ?: throw InvalidCredentialsException()

        if (user.isDeleted()) {
            throw InvalidCredentialsException()
        }

        if (!passwordEncoder.matches(password, user.password)) {
            throw InvalidCredentialsException()
        }

        authenticationAccessSessionRepository.revokeAllByUserId(user.id, "NEW_SIGN_IN")

        val now = Instant.now()
        val authenticationAccessSession =
            AuthenticationAccessSession(
                id = 0,
                authenticationAccessSession = UUID.randomUUID(),
                userId = user.id,
                createdAt = now,
                expiresAt = now.plusSeconds(authProperties.accessSession.expiryDays * 24 * 60 * 60),
                revokedAt = null,
                revokedReason = null,
            )

        return authenticationAccessSessionRepository.save(authenticationAccessSession)
    }
}
