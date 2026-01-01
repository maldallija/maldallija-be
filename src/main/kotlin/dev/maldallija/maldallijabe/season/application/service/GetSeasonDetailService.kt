package dev.maldallija.maldallijabe.season.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.season.application.port.`in`.GetSeasonDetailUseCase
import dev.maldallija.maldallijabe.season.application.port.`in`.dto.SeasonDetail
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetSeasonDetailService(
    private val seasonRepository: SeasonRepository,
    private val equestrianCenterRepository: EquestrianCenterRepository,
) : GetSeasonDetailUseCase {
    override fun getSeasonDetail(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
    ): SeasonDetail {
        // 1. 승마장 조회
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 2. 시즌 조회
        val season =
            seasonRepository.findByUuid(seasonUuid)
                ?: throw SeasonNotFoundException()

        // 3. 시즌이 해당 승마장에 속하는지 검증
        if (season.equestrianCenterId != equestrianCenter.id) {
            throw SeasonNotFoundException()
        }

        // 4. DTO 변환
        return SeasonDetail(
            seasonUuid = season.uuid,
            equestrianCenterUuid = equestrianCenter.uuid,
            equestrianCenterName = equestrianCenter.name,
            title = season.title,
            description = season.description,
            startDate = season.startDate,
            endDate = season.endDate,
            capacity = season.capacity,
            defaultTicketCount = season.defaultTicketCount,
            status = season.status,
            createdAt = season.createdAt,
            updatedAt = season.updatedAt,
        )
    }
}
