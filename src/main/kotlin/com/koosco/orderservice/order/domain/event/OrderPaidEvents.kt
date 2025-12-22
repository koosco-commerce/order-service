package com.koosco.orderservice.order.domain.event

import com.koosco.common.core.event.AbstractDomainEvent

/**
 * 주문이 결제 완료되었음을 나타내는 도메인 이벤트
 */
data class OrderPaidEvent(val orderId: Long, val paidAmount: Long, val items: List<OrderItemInfo>) :
    AbstractDomainEvent() {
    override fun getEventType(): String = "OrderPaid"

    override fun getAggregateId(): String = orderId.toString()
}

data class OrderCancelledEvent(val orderId: Long, val reason: OrderCancelReason, val items: List<OrderItemInfo>) :
    AbstractDomainEvent() {

    override fun getEventType(): String = "OrderCancelled"

    override fun getAggregateId(): String = orderId.toString()
}

enum class OrderCancelReason {
    USER_REQUEST,
    PAYMENT_TIMEOUT,
    PAYMENT_FAILED,
}
