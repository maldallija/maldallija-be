package dev.maldallija.maldallijabe.notification.adapter.out.discord.dto

data class DiscordEmbed(
    val title: String,
    val color: Int,
    val fields: List<DiscordField>,
)
