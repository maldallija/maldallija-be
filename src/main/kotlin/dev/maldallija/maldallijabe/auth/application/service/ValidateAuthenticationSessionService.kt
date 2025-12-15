package dev.maldallija.maldallijabe.auth.application.service

import dev.maldallija.maldallijabe.auth.application.port.`in`.ValidateAuthenticationSessionUseCase
import dev.maldallija.maldallijabe.auth.application.port.out.AuthenticationAccessSessionRepository
import dev.maldallija.maldallijabe.auth.domain.exception.InvalidSessionException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ValidateAuthenticationSessionService(
    private val authenticationAccessSessionRepository: AuthenticationAccessSessionRepository,
) : ValidateAuthenticationSessionUseCase {
    override fun validateAuthenticationSession(authenticationAccessSessionId: UUID): Long {
        val authenticationAccessSession =
            authenticationAccessSessionRepository.findByAuthenticationAccessSession(authenticationAccessSessionId)
                ?: throw InvalidSessionException()

        if (!authenticationAccessSession.isValid()) {
            throw InvalidSessionException()
        }

        return authenticationAccessSession.userId
    }
}
