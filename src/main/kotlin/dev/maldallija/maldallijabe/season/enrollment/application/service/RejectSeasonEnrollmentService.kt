package dev.maldallija.maldallijabe.season.enrollment.application.service

import dev.maldallija.maldallijabe.equestriancenter.center.application.port.out.EquestrianCenterRepository
import dev.maldallija.maldallijabe.equestriancenter.center.domain.exception.EquestrianCenterNotFoundException
import dev.maldallija.maldallijabe.equestriancenter.staff.application.port.out.EquestrianCenterStaffRepository
import dev.maldallija.maldallijabe.season.application.port.out.SeasonRepository
import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import dev.maldallija.maldallijabe.season.domain.exception.SeasonNotFoundException
import dev.maldallija.maldallijabe.season.enrollment.application.port.`in`.RejectSeasonEnrollmentUseCase
import dev.maldallija.maldallijabe.season.enrollment.application.port.out.SeasonEnrollmentRepository
import dev.maldallija.maldallijabe.season.enrollment.domain.EnrollmentStatus
import dev.maldallija.maldallijabe.season.enrollment.domain.exception.CannotProcessEnrollmentForClosedSeasonException
import dev.maldallija.maldallijabe.season.enrollment.domain.exception.InvalidEnrollmentStatusException
import dev.maldallija.maldallijabe.season.enrollment.domain.exception.SeasonEnrollmentNotFoundException
import dev.maldallija.maldallijabe.season.enrollment.domain.exception.UnauthorizedSeasonEnrollmentOperationException
import dev.maldallija.maldallijabe.season.enrollmentlog.application.port.out.SeasonEnrollmentLogRepository
import dev.maldallija.maldallijabe.season.enrollmentlog.domain.EnrollmentLogType
import dev.maldallija.maldallijabe.season.enrollmentlog.domain.SeasonEnrollmentLog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class RejectSeasonEnrollmentService(
    private val equestrianCenterRepository: EquestrianCenterRepository,
    private val seasonRepository: SeasonRepository,
    private val seasonEnrollmentRepository: SeasonEnrollmentRepository,
    private val seasonEnrollmentLogRepository: SeasonEnrollmentLogRepository,
    private val equestrianCenterStaffRepository: EquestrianCenterStaffRepository,
) : RejectSeasonEnrollmentUseCase {
    override fun rejectSeasonEnrollment(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        enrollmentUuid: UUID,
        requestingUserId: Long,
        note: String?,
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

        // 4. 시즌 상태 확인 (ACTIVE만 허용)
        if (season.status != SeasonStatus.ACTIVE) {
            throw CannotProcessEnrollmentForClosedSeasonException()
        }

        // 5. Staff 권한 확인
        equestrianCenterStaffRepository.findActiveByEquestrianCenterIdAndUserId(
            equestrianCenterId = equestrianCenter.id,
            userId = requestingUserId,
        ) ?: throw UnauthorizedSeasonEnrollmentOperationException()

        // 6. SeasonEnrollment 조회
        val enrollment =
            seasonEnrollmentRepository.findByUuid(enrollmentUuid)
                ?: throw SeasonEnrollmentNotFoundException()

        // 7. Enrollment이 해당 시즌에 속하는지 확인
        if (enrollment.seasonId != season.id) {
            throw SeasonEnrollmentNotFoundException()
        }

        // 8. 상태가 PENDING인지 확인
        if (enrollment.enrollmentStatus != EnrollmentStatus.PENDING) {
            throw InvalidEnrollmentStatusException()
        }

        // 9. PENDING → REJECTED 전환
        val now = Instant.now()
        val rejectedEnrollment =
            enrollment.copy(
                enrollmentStatus = EnrollmentStatus.REJECTED,
                updatedAt = now,
            )
        seasonEnrollmentRepository.save(rejectedEnrollment)

        // 10. SeasonEnrollmentLog 생성 (REJECTED)
        val enrollmentLog =
            SeasonEnrollmentLog(
                id = 0L,
                seasonEnrollmentId = enrollment.id,
                enrollmentLogType = EnrollmentLogType.REJECTED,
                actorId = requestingUserId,
                note = note,
                createdAt = now,
            )
        seasonEnrollmentLogRepository.save(enrollmentLog)
    }
}
