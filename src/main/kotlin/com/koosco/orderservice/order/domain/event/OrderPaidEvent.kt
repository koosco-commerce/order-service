package com.koosco.orderservice.order.domain.event

import com.koosco.common.core.event.AbstractDomainEvent

/**
 * 주문이 결제 완료되었음을 나타내는 도메인 이벤트
 */
data class OrderPaidEvent(val orderId: Long, val paidAmount: Long, val items: List<OrderItemInfo>) :
    AbstractDomainEvent() {
    override fun getEventType(): String = "com.koosco.order.paid"
    override fun getAggregateId(): String = orderId.toString()
}
