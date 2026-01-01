package dev.maldallija.maldallijabe.season.adapter.out.persistence

import dev.maldallija.maldallijabe.season.domain.SeasonStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID

interface SeasonJpaRepository : JpaRepository<SeasonEntity, Long> {
    fun findByUuidAndDeletedAtIsNull(uuid: UUID): SeasonEntity?

    fun findByEquestrianCenterIdAndDeletedAtIsNull(
        equestrianCenterId: Long,
        pageable: Pageable,
    ): Page<SeasonEntity>

    fun findByEquestrianCenterIdAndStatusAndDeletedAtIsNull(
        equestrianCenterId: Long,
        status: SeasonStatus,
        pageable: Pageable,
    ): Page<SeasonEntity>

    @Query(
        """
        SELECT s FROM SeasonEntity s
        WHERE s.equestrianCenterId = :equestrianCenterId
          AND s.startDate <= :searchEndDate
          AND s.endDate >= :searchStartDate
          AND s.deletedAt IS NULL
        """,
    )
    fun findByEquestrianCenterIdAndDateRangeAndDeletedAtIsNull(
        @Param("equestrianCenterId") equestrianCenterId: Long,
        @Param("searchStartDate") searchStartDate: LocalDate,
        @Param("searchEndDate") searchEndDate: LocalDate,
        pageable: Pageable,
    ): Page<SeasonEntity>

    @Query(
        """
        SELECT s FROM SeasonEntity s
        WHERE s.equestrianCenterId = :equestrianCenterId
          AND s.status = :status
          AND s.startDate <= :searchEndDate
          AND s.endDate >= :searchStartDate
          AND s.deletedAt IS NULL
        """,
    )
    fun findByEquestrianCenterIdAndStatusAndDateRangeAndDeletedAtIsNull(
        @Param("equestrianCenterId") equestrianCenterId: Long,
        @Param("status") status: SeasonStatus,
        @Param("searchStartDate") searchStartDate: LocalDate,
        @Param("searchEndDate") searchEndDate: LocalDate,
        pageable: Pageable,
    ): Page<SeasonEntity>
}
