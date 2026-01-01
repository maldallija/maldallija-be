package dev.maldallija.maldallijabe.season.application.port.out

import dev.maldallija.maldallijabe.season.domain.Season

interface SeasonRepository {
    fun save(season: Season): Season
}
