package dev.maldallija.maldallijabe.season.enrollment.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import dev.maldallija.maldallijabe.season.enrollment.application.port.`in`.ApplyToSeasonUseCase
import dev.maldallija.maldallijabe.season.enrollment.application.port.out.SeasonEnrollmentRepository
import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
import dev.maldallija.maldallijabe.season.enrollment.domain.SeasonEnrollment
import dev.maldallija.maldallijabe.season.enrollment.domain.exception.CannotApplyToInactiveSeasonException
import dev.maldallija.maldallijabe.season.enrollment.domain.exception.DuplicateEnrollmentException
import dev.maldallija.maldallijabe.season.enrollmentlog.application.port.out.SeasonEnrollmentLogRepository
import dev.maldallija.maldallijabe.season.enrollmentlog.domain.EnrollmentLogType
import dev.maldallija.maldallijabe.season.enrollmentlog.domain.SeasonEnrollmentLog
import dev.maldallija.maldallijabe.user.application.port.out.UserRepository
import dev.maldallija.maldallijabe.user.domain.exception.UserNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class ApplyToSeasonService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val seasonRepository: SeasonRepository,
    private val userRepository: UserRepository,
    private val seasonEnrollmentRepository: SeasonEnrollmentRepository,
    private val seasonEnrollmentLogRepository: SeasonEnrollmentLogRepository,
) : ApplyToSeasonUseCase {
    override fun applyToSeason(
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

        // 4. 시즌 상태 확인 (ACTIVE 시즌만 신청 가능)
        if (season.status != SeasonStatus.ACTIVE) {
            throw CannotApplyToInactiveSeasonException()
        }

        // 5. 신청자 존재 확인
        val member =
            userRepository.findById(requestingUserId)
                ?: throw UserNotFoundException()

        // 6. 중복 신청 방지 (PENDING 또는 APPROVED 상태가 이미 있는지 확인)
        val hasPending =
            seasonEnrollmentRepository.findBySeasonIdAndMemberIdAndStatus(
                seasonId = season.id,
                memberId = member.id,
                enrollmentStatus = EnrollmentStatus.PENDING,
            ) != null
        val hasApproved =
            seasonEnrollmentRepository.findBySeasonIdAndMemberIdAndStatus(
                seasonId = season.id,
                memberId = member.id,
                enrollmentStatus = EnrollmentStatus.APPROVED,
            ) != null

        if (hasPending || hasApproved) {
            throw DuplicateEnrollmentException()
        }

        // 7. 재신청 여부 확인 (과거에 enrollment가 있었는지)
        val hasAnyPreviousEnrollment =
            seasonEnrollmentRepository.existsBySeasonIdAndMemberId(
                seasonId = season.id,
                memberId = member.id,
            )

        val enrollmentLogType =
            if (hasAnyPreviousEnrollment) {
                EnrollmentLogType.REAPPLIED
            } else {
                EnrollmentLogType.APPLIED
            }

        // 8. SeasonEnrollment 생성
        val now = Instant.now()
        val seasonEnrollment =
            SeasonEnrollment(
                id = 0L,
                uuid = UUID.randomUUID(),
                seasonId = season.id,
                memberId = member.id,
                enrollmentStatus = EnrollmentStatus.PENDING,
                createdAt = now,
                updatedAt = now,
            )

        val savedEnrollment = seasonEnrollmentRepository.save(seasonEnrollment)

        // 9. SeasonEnrollmentLog 생성
        val seasonEnrollmentLog =
            SeasonEnrollmentLog(
                id = 0L,
                seasonEnrollmentId = savedEnrollment.id,
                enrollmentLogType = enrollmentLogType,
                actorId = member.id,
                note = null,
                createdAt = now,
            )

        seasonEnrollmentLogRepository.save(seasonEnrollmentLog)
    }
}
