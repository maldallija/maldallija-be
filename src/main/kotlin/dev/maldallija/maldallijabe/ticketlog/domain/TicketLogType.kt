package dev.maldallija.maldallijabe.ticketlog.domain

enum class TicketLogType {
    GRANT, // 참여 승인 시 기본 티켓 부여
    USE, // 레슨 예약 시 차감
    REFUND, // 예약 취소 시 환불
    ADDITIONAL, // 지도사가 추가 부여
}
