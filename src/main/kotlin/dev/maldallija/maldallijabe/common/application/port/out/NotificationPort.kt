package dev.maldallija.maldallijabe.common.application.port.out

import dev.maldallija.maldallijabe.common.domain.ExceptionEvent

interface NotificationPort {
    fun notifyException(event: ExceptionEvent)
}
