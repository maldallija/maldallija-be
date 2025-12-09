package dev.maldallija.maldallijabe.common.domain

import java.time.Instant

data class ExceptionEvent(
    val timestamp: Instant,
    val statusCode: Int,
    val method: String,
    val path: String,
    val errorMessage: String?,
    val userAgent: String?,
)
