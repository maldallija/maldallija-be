package dev.maldallija.maldallijabe.auth.application.service

import dev.maldallija.maldallijabe.auth.application.port.`in`.ValidateSessionUseCase
import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationAccessSessionRepository
import dev.maldallija.maldallijabe.auth.domain.exception.InvalidSessionException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ValidateSessionService(
    private val authenticationAccessSessionRepository: AuthenticationAccessSessionRepository,
) : ValidateSessionUseCase {
    override fun validateSession(sessionId: UUID): Long {
        val session =
            authenticationAccessSessionRepository.findByAuthenticationAccessSession(sessionId)
                ?: throw InvalidSessionException()

        if (!session.isValid()) {
            throw InvalidSessionException()
        }

        return session.userId
    }
}
