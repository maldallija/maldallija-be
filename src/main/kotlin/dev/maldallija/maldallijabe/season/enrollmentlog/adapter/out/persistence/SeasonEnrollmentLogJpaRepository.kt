package dev.maldallija.maldallijabe.season.enrollmentlog.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface SeasonEnrollmentLogJpaRepository : JpaRepository<SeasonEnrollmentLogEntity, Long>
