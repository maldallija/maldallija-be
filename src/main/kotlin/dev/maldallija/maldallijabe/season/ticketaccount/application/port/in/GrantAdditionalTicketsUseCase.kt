package dev.maldallija.maldallijabe.season.ticketaccount.application.port.`in`

import java.util.UUID

interface GrantAdditionalTicketsUseCase {
    fun grantAdditionalTickets(
        equestrianCenterUuid: UUID,
        seasonUuid: UUID,
        enrollmentUuid: UUID,
        requestingUserId: Long,
        amount: Int,
        description: String?,
    )
}
