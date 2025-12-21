package dev.maldallija.maldallijabe.notification.adapter.out.discord

import dev.maldallija.maldallijabe.common.domain.ExceptionEvent
import dev.maldallija.maldallijabe.notification.adapter.out.discord.dto.DiscordEmbed
import dev.maldallija.maldallijabe.notification.adapter.out.discord.dto.DiscordField
import dev.maldallija.maldallijabe.notification.adapter.out.discord.dto.DiscordWebhookRequest
import dev.maldallija.maldallijabe.notification.application.port.out.NotificationPort
import org.springframework.web.client.RestClient

class DiscordNotificationAdapter(
    private val webhookUrl: String,
    private val restClient: RestClient,
) : NotificationPort {
    override fun notifyException(event: ExceptionEvent) {
        val webhookRequest = buildDiscordWebhookRequest(event)

        try {
            restClient
                .post()
                .uri(webhookUrl)
                .body(webhookRequest)
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            // 알림 실패해도 원래 요청은 정상 응답
        }
    }

    private fun buildDiscordWebhookRequest(event: ExceptionEvent): DiscordWebhookRequest {
        val fields =
            listOf(
                DiscordField(name = "Status Code", value = event.statusCode.toString(), inline = true),
                DiscordField(name = "Method", value = event.method, inline = true),
                DiscordField(name = "Path", value = event.path, inline = false),
                DiscordField(name = "Timestamp", value = event.timestamp.toString(), inline = false),
                DiscordField(name = "User-Agent", value = event.userAgent ?: "N/A", inline = false),
                DiscordField(name = "Error Message", value = event.errorMessage ?: "No message", inline = false),
            )

        val embed =
            DiscordEmbed(
                title = "🚨 Server Error (${event.statusCode})",
                color = 15158332,
                fields = fields,
            )

        return DiscordWebhookRequest(embeds = listOf(embed))
    }
}
