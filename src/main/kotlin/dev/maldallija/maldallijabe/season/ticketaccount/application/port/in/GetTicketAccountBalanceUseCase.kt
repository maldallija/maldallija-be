package dev.maldallija.maldallijabe.season.ticketaccount.application.port.`in`

import java.util.UUID

interface GetTicketAccountBalanceUseCase {
    fun getTicketAccountBalance(
        userUuid: UUID,
        seasonUuid: UUID,
        requestingUserId: Long,
    ): Int
}
