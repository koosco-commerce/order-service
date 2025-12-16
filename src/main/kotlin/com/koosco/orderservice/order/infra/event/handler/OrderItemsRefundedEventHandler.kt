package com.koosco.orderservice.order.infra.event.handler

import com.koosco.common.core.event.DomainEvent
import com.koosco.orderservice.order.application.event.RefundItem
import com.koosco.orderservice.order.application.event.RefundRequested
import com.koosco.orderservice.order.application.port.outbound.RefundEventPublishPort
import com.koosco.orderservice.order.domain.event.OrderItemsRefundedEvent
import org.springframework.stereotype.Component

/**
 * OrderItemsRefundedEvent를 처리하여 환불 요청 이벤트를 발행하는 핸들러
 */
@Component
class OrderItemsRefundedEventHandler(private val refundEventPublishPort: RefundEventPublishPort) :
    DomainEventHandler<OrderItemsRefundedEvent> {

    override fun supports(event: DomainEvent): Boolean = event is OrderItemsRefundedEvent

    override fun handle(event: OrderItemsRefundedEvent) {
        // 환불 요청 이벤트 발행 (PG 환불 요청, 재고 복구 등)
        val refundEvent = RefundRequested(
            orderId = event.orderId,
            refundAmount = event.refundedAmount,
            items = event.refundedItems.map {
                RefundItem(
                    productId = it.productId,
                    quantity = it.quantity,
                    refundAmount = it.refundAmount,
                )
            },
        )
        refundEventPublishPort.publishRefundRequest(refundEvent)
    }
}
