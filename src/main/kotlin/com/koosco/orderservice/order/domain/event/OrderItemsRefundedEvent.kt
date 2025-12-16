package com.koosco.orderservice.order.domain.event

import com.koosco.common.core.event.AbstractDomainEvent

/**
 * 주문 아이템이 환불되었음을 나타내는 도메인 이벤트
 */
data class OrderItemsRefundedEvent(
    val orderId: Long,
    val refundedAmount: Long,
    val refundedItems: List<RefundedItemInfo>,
) : AbstractDomainEvent() {
    override fun getEventType(): String = "com.koosco.order.items.refunded"
    override fun getAggregateId(): String = orderId.toString()
}

data class RefundedItemInfo(val productId: Long, val quantity: Int, val refundAmount: Long)
