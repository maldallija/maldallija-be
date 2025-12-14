package dev.maldallija.maldallijabe.auth.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface AuthenticationAccessSessionJpaRepository : JpaRepository<AuthenticationAccessSessionEntity, Long> {
    fun findByAccessToken(accessToken: UUID): AuthenticationAccessSessionEntity?

    @Modifying
    @Query(
        """
        UPDATE AuthenticationAccessSessionEntity a
        SET a.revokedAt = :revokedAt, a.revokedReason = :reason
        WHERE a.userId = :userId AND a.revokedAt IS NULL
        """,
    )
    fun revokeAllByUserId(
        @Param("userId") userId: Long,
        @Param("reason") reason: String,
        @Param("revokedAt") revokedAt: Instant,
    )
}
