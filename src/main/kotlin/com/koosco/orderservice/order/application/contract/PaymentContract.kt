package com.koosco.orderservice.order.application.contract

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import org.jetbrains.annotations.NotNull

/**
 * fileName       : PaymentContract
 * author         : koo
 * date           : 2025. 12. 23. 오전 2:50
 * description    :
 */

/**
 * 결제 취소
 */
data class PaymentCancelledEvent(
    @field:NotNull
    val orderId: Long,

    @field:NotBlank
    val paymentId: String,

    val transactionId: String? = null,

    /**
     * 취소/환불 금액 (부분취소면 일부 금액)
     */
    @field:PositiveOrZero
    val cancelledAmount: Long,

    @field:NotBlank
    val currency: String = "KRW",

    /**
     * 취소 사유 코드 (예: USER_CANCELLED, PAYMENT_TIMEOUT, STOCK_CONFIRM_FAILED, REFUND_REQUESTED)
     */
    @field:NotBlank
    val reason: String,

    /**
     * 취소 완료 시각 (epoch millis)
     */
    @field:Positive
    val cancelledAt: Long,

    @field:NotBlank
    val correlationId: String,

    val causationId: String? = null,
)

/**
 * 결제 승인
 */
data class PaymentCompletedEvent(
    @field:NotNull
    val orderId: Long,

    /**
     * payment-service 내부 결제 식별자 (멱등/재처리에 매우 유용)
     */
    @field:NotBlank
    val paymentId: String,

    /**
     * PG 승인/거래 식별자 (있으면 강추, 없으면 null 허용)
     */
    val transactionId: String? = null,

    @field:PositiveOrZero
    val paidAmount: Long,

    @field:NotBlank
    val currency: String = "KRW",

    /**
     * 승인 시각 (epoch millis)
     */
    @field:Positive
    val approvedAt: Long,

    /**
     * 사가 추적용 (권장: orderId 문자열로 통일)
     */
    @field:NotBlank
    val correlationId: String,

    /**
     * 직전 원인 메시지 id (보통 payment.create.requested의 CloudEvent.id)
     */
    val causationId: String? = null,
)
