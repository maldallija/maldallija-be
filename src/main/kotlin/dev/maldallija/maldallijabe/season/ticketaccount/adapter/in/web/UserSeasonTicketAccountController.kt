package dev.maldallija.maldallijabe.season.ticketaccount.adapter.`in`.web

import dev.maldallija.maldallijabe.season.ticketaccount.adapter.`in`.web.dto.TicketAccountBalanceResponse
import dev.maldallija.maldallijabe.season.ticketaccount.application.port.`in`.GetTicketAccountBalanceUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Tag(name = "User - Season Ticket Account", description = "사용자 시즌 티켓 계좌 API")
@RestController
@RequestMapping("/api/v1/users")
class UserSeasonTicketAccountController(
    private val getTicketAccountBalanceUseCase: GetTicketAccountBalanceUseCase,
) {
    @Operation(summary = "티켓 잔액 조회", description = "시즌의 티켓 잔액을 조회합니다 (본인만 조회 가능)")
    @GetMapping("/{userUuid}/seasons/{seasonUuid}/ticket-account")
    fun getTicketAccountBalance(
        @PathVariable userUuid: UUID,
        @PathVariable seasonUuid: UUID,
        @AuthenticationPrincipal requestingUserId: Long,
    ): ResponseEntity<TicketAccountBalanceResponse> {
        val balance =
            getTicketAccountBalanceUseCase.getTicketAccountBalance(
                userUuid = userUuid,
                seasonUuid = seasonUuid,
                requestingUserId = requestingUserId,
            )

        return ResponseEntity.ok(TicketAccountBalanceResponse(balance = balance))
    }
}
