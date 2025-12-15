package dev.maldallija.maldallijabe.auth.application.service

import dev.maldallija.maldallijabe.auth.application.port.`in`.SignInUseCase
import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationAccessSessionRepository
import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationRefreshSessionRepository
import dev.maldallija.maldallijabe.auth.config.AuthProperties
import dev.maldallija.maldallijabe.auth.domain.AuthenticationAccessSession
import dev.maldallija.maldallijabe.auth.domain.AuthenticationRefreshSession
import dev.maldallija.maldallijabe.auth.domain.SignInResult
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
    private val authenticationRefreshSessionRepository: AuthenticationRefreshSessionRepository,
) : SignInUseCase {
    override fun signIn(
        username: String,
        password: String,
    ): SignInResult {
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
        authenticationRefreshSessionRepository.revokeAllByUserId(user.id, "NEW_SIGN_IN")

        val now = Instant.now()
        val authenticationAccessSession =
            AuthenticationAccessSession(
                id = 0,
                authenticationAccessSession = UUID.randomUUID(),
                userId = user.id,
                createdAt = now,
                expiresAt = now.plusSeconds(authProperties.accessSession.expiryHours * 60 * 60),
                revokedAt = null,
                revokedReason = null,
            )

        val authenticationRefreshSession =
            AuthenticationRefreshSession(
                id = 0,
                authenticationRefreshSession = UUID.randomUUID(),
                userId = user.id,
                createdAt = now,
                expiresAt = now.plusSeconds(authProperties.refreshSession.expiryDays * 24 * 60 * 60),
                revokedAt = null,
                revokedReason = null,
            )

        val savedAccessSession = authenticationAccessSessionRepository.save(authenticationAccessSession)
        val savedRefreshSession = authenticationRefreshSessionRepository.save(authenticationRefreshSession)

        return SignInResult(
            accessSession = savedAccessSession,
            refreshSession = savedRefreshSession,
        )
    }
}
