package dev.maldallija.maldallijabe.equestriancenter.application.service

import dev.maldallija.maldallijabe.equestriancenter.application.port.`in`.CreateEquestrianCenterUseCase
import dev.maldallija.maldallijabe.equestriancenter.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.domain.EquestrianCenter
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
        administratorId: Long,
        name: String,
        description: String?,
        leaderUserUuid: UUID,
    ) {
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
                createdBy = administratorId,
                createdAt = now,
                updatedBy = administratorId,
                updatedAt = now,
                deletedAt = null,
            )

        equestrianCenterRepository.save(equestrianCenter)
    }
}
