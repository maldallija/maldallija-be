package dev.maldallija.maldallijabe.auth.application.service

import dev.maldallija.maldallijabe.auth.application.port.`in`.SignInUseCase
import dev.maldallija.maldallijabe.auth.domain.exception.InvalidCredentialsException
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SignInService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : SignInUseCase {
    override fun signIn(
        username: String,
        password: String,
    ) {
        val user =
            userRepository.findByUsername(username)
                ?: throw InvalidCredentialsException()

        if (user.isDeleted()) {
            throw InvalidCredentialsException()
        }

        if (!passwordEncoder.matches(password, user.password)) {
            throw InvalidCredentialsException()
        }

        // TODO-noah: refresh session 반환

        // TODO-noah: access session 반환
    }
}
