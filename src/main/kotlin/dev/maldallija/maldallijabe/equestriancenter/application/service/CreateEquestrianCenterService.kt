package dev.maldallija.maldallijabe.equestriancenter.application.service

import dev.maldallija.maldallijabe.equestriancenter.application.port.`in`.CreateEquestrianCenterUseCase
import dev.maldallija.maldallijabe.equestriancenter.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.domain.EquestrianCenter
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.UnauthorizedEquestrianCenterOperationException
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import dev.maldallija.maldallijabe.user.domain.exception.UserNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class CreateEquestrianCenterService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val userRepository: UserRepository,
) : CreateEquestrianCenterUseCase {
    override fun createEquestrianCenter(
        name: String,
        description: String?,
        leaderUserUuid: UUID,
        requestingUserId: Long,
    ) {
        val requestingUser =
            userRepository.findById(requestingUserId)
                ?: throw UserNotFoundException()

        // TODO-noah: 해당 부분 나중에 확인
        if (!requestingUser.isSystemAdmin) {
            throw UnauthorizedEquestrianCenterOperationException()
        }

        val leaderUser =
            userRepository.findByUuid(leaderUserUuid)
                ?: throw UserNotFoundException()

        val now = Instant.now()

        val equestrianCenter =
            EquestrianCenter(
                id = 0,
                uuid = UUID.randomUUID(),
                name = name,
                description = description,
                leaderUserId = leaderUser.id,
                createdBy = requestingUser.id,
                createdAt = now,
                updatedBy = requestingUser.id,
                updatedAt = now,
                deletedAt = null,
            )

        equestrianCenterRepository.save(equestrianCenter)
    }
}
