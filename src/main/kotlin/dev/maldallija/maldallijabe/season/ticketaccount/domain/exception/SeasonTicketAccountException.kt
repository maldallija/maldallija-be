package dev.maldallija.maldallijabe.season.ticketaccount.domain.exception

import dev.maldallija.maldallijabe.common.domain.exception.BaseException

sealed class SeasonTicketAccountException(
    errorCode: String,
    message: String,
) : BaseException(errorCode, message)

class DuplicateTicketAccountException :
    SeasonTicketAccountException(
        errorCode = "DUPLICATE_TICKET_ACCOUNT",
        message = "Ticket account already exists for this season and member",
    )

class TicketAccountNotFoundException :
    SeasonTicketAccountException(
        errorCode = "TICKET_ACCOUNT_NOT_FOUND",
        message = "Ticket account not found",
    )
