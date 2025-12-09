package dev.maldallija.maldallijabe.common.adapter.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "discord.webhook.exception")
data class DiscordProperties(
    val url: String,
)
