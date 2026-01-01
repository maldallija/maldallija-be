package dev.maldallija.maldallijabe.season.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface SeasonJpaRepository : JpaRepository<SeasonEntity, Long>
