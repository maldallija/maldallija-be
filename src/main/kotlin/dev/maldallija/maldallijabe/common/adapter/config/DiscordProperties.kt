package dev.maldallija.maldallijabe.common.adapter.config

import dev.maldallija.maldallijabe.common.config.Environment
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile

@Profile(Environment.DEVELOPMENT, Environment.PRODUCTION)
@ConfigurationProperties(prefix = "discord.webhook.exception")
data class DiscordProperties(
    val url: String,
)
