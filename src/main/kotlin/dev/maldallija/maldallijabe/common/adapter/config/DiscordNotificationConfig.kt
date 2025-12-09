package dev.maldallija.maldallijabe.common.adapter.config

import dev.maldallija.maldallijabe.common.adapter.out.notification.DiscordNotificationAdapter
import dev.maldallija.maldallijabe.common.application.port.out.NotificationPort
import dev.maldallija.maldallijabe.common.config.Environment
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestClient

@Profile(Environment.PRODUCTION)
@Configuration
@EnableConfigurationProperties(DiscordProperties::class)
class DiscordNotificationConfig {
    @Bean
    fun discordNotificationAdapter(properties: DiscordProperties): NotificationPort {
        val restClient = RestClient.create()
        return DiscordNotificationAdapter(properties.url, restClient)
    }
}
