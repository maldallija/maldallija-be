package dev.maldallija.maldallijabe.notification.application.port.out

import dev.maldallija.maldallijabe.common.domain.ExceptionEvent

interface NotificationPort {
    fun notifyException(event: ExceptionEvent)
}
