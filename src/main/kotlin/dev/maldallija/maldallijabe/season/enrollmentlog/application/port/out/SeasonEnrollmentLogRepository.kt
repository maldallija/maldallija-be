package dev.maldallija.maldallijabe.season.enrollmentlog.application.port.out

import dev.maldallija.maldallijabe.season.enrollmentlog.domain.SeasonEnrollmentLog

interface SeasonEnrollmentLogRepository {
    fun save(seasonEnrollmentLog: SeasonEnrollmentLog): SeasonEnrollmentLog
}
