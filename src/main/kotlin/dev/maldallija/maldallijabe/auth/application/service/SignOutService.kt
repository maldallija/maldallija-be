package dev.maldallija.maldallijabe.auth.application.service

import dev.maldallija.maldallijabe.auth.application.port.`in`.SignOutUseCase
import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationAccessSessionRepository
import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationRefreshSessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SignOutService(
    private val authenticationRefreshSessionRepository: AuthenticationRefreshSessionRepository,
    private val authenticationAccessSessionRepository: AuthenticationAccessSessionRepository,
) : SignOutUseCase {
    override fun signOut(userId: Long) {
        authenticationRefreshSessionRepository.revokeAllByUserId(userId, "SIGN_OUT")
        authenticationAccessSessionRepository.revokeAllByUserId(userId, "SIGN_OUT")
    }
}
