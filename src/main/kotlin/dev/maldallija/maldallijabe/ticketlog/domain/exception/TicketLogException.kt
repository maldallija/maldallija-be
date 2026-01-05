package dev.maldallija.maldallijabe.ticketlog.domain.exception

import dev.maldallija.maldallijabe.common.domain.exception.BaseException

sealed class TicketLogException(
    errorCode: String,
    message: String,
) : BaseException(errorCode, message)

class TicketLogNotFoundException :
    TicketLogException(
        errorCode = "TICKET_LOG_NOT_FOUND",
        message = "Ticket log not found",
    )
