package com.koosco.orderservice.order.domain.event

import com.koosco.common.core.event.AbstractDomainEvent

/**
 * 주문이 생성되었음을 나타내는 도메인 이벤트
 */
data class OrderCreatedEvent(
    val orderId: Long,
    val userId: Long,
    val totalAmount: Long,
    val payableAmount: Long,
    val items: List<OrderItemInfo>,
) : AbstractDomainEvent() {
    override fun getEventType(): String = "com.koosco.order.created"
    override fun getAggregateId(): String = orderId.toString()
}

data class OrderItemInfo(val productId: Long, val quantity: Int, val unitPrice: Long)
