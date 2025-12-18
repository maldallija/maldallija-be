package dev.maldallija.maldallijabe.equestriancenter.application.service

import dev.maldallija.maldallijabe.equestriancenter.application.port.`in`.EquestrianCenterDetail
import dev.maldallija.maldallijabe.equestriancenter.application.port.`in`.GetEquestrianCenterUseCase
import dev.maldallija.maldallijabe.equestriancenter.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import dev.maldallija.maldallijabe.user.domain.exception.UserNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetEquestrianCenterService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val userRepository: UserRepository,
) : GetEquestrianCenterUseCase {
    override fun getEquestrianCenter(equestrianCenterUuid: UUID): EquestrianCenterDetail {
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        val representativeUser =
            userRepository.findById(equestrianCenter.representativeUserId)
                ?: throw UserNotFoundException()

        return EquestrianCenterDetail(
            uuid = equestrianCenter.uuid,
            name = equestrianCenter.name,
            description = equestrianCenter.description,
            representativeUserUuid = representativeUser.uuid,
            createdAt = equestrianCenter.createdAt,
            updatedAt = equestrianCenter.updatedAt,
        )
    }
}
