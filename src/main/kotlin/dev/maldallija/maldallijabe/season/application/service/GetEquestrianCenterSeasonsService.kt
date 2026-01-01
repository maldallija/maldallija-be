package dev.maldallija.maldallijabe.season.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.season.application.port.`in`.GetEquestrianCenterSeasonsUseCase
import dev.maldallija.maldallijabe.season.application.port.`in`.dto.SeasonSummary
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import dev.maldallija.maldallijabe.season.domain.exception.InvalidSearchDateRangeException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetEquestrianCenterSeasonsService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val seasonRepository: SeasonRepository,
) : GetEquestrianCenterSeasonsUseCase {
    override fun getEquestrianCenterSeasons(
        equestrianCenterUuid: UUID,
        status: SeasonStatus?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        pageable: Pageable,
    ): Page<SeasonSummary> {
        // 1. 날짜 검색 조건 유효성 검사
        if (startDate != null || endDate != null) {
            if (startDate == null || endDate == null) {
                throw InvalidSearchDateRangeException("검색 시작일과 종료일은 모두 제공해야 합니다.")
            }
            if (endDate.isBefore(startDate)) {
                throw InvalidSearchDateRangeException("검색 종료일은 시작일보다 이후여야 합니다.")
            }
        }

        // 2. 승마장 존재 확인
        val equestrianCenter =
            equestrianCenterRepository.findByUuid(equestrianCenterUuid)
                ?: throw EquestrianCenterNotFoundException()

        // 3. 시즌 목록 조회
        val seasons =
            seasonRepository.findByEquestrianCenterIdAndSearchConditions(
                equestrianCenterId = equestrianCenter.id,
                status = status,
                searchStartDate = startDate,
                searchEndDate = endDate,
                pageable = pageable,
            )

        // 4. DTO 변환
        return seasons.map { season ->
            SeasonSummary(
                seasonUuid = season.uuid,
                title = season.title,
                startDate = season.startDate,
                endDate = season.endDate,
                capacity = season.capacity,
                status = season.status,
            )
        }
    }
}
