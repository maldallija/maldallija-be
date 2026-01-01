package dev.maldallija.maldallijabe.season.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.season.application.port.`in`.CloseSeasonUseCase
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import dev.maldallija.maldallijabe.season.domain.exception.CannotCloseInactiveSeasonException
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import dev.maldallija.maldallijabe.season.domain.exception.UnauthorizedSeasonOperationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class CloseSeasonService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val seasonRepository: SeasonRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
) : CloseSeasonUseCase {
    override fun closeSeason(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        requestingUserId: Long,
    ) {
        // 1. 승마장 존재 확인
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

        // 4. 직원 권한 확인
        val staff =
            equestrianCenterStaffRepository.findActiveByEquestrianCenterIdAndUserId(
                equestrianCenterId = equestrianCenter.id,
                userId = requestingUserId,
            ) ?: throw UnauthorizedSeasonOperationException()

        // 5. 시즌 상태 확인 (ACTIVE 시즌만 종료 가능)
        if (season.status != SeasonStatus.ACTIVE) {
            throw CannotCloseInactiveSeasonException()
        }

        // 6. 시즌 종료
        val closedSeason =
            season.copy(
                status = SeasonStatus.CLOSED,
                updatedBy = staff.id,
                updatedAt = Instant.now(),
            )

        seasonRepository.save(closedSeason)
    }
}
