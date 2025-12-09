package dev.maldallija.maldallijabe.common.adapter.out.notification.dto

data class DiscordEmbed(
    val title: String,
    val color: Int,
    val fields: List<DiscordField>,
)
