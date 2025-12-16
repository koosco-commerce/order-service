package com.koosco.orderservice.order.infra.event.handler

import com.koosco.common.core.event.DomainEvent
import com.koosco.orderservice.order.application.event.InventoryConfirmItem
import com.koosco.orderservice.order.application.event.InventoryConfirmRequested
import com.koosco.orderservice.order.application.port.outbound.InventoryEventPublishPort
import com.koosco.orderservice.order.domain.event.OrderPaidEvent
import org.springframework.stereotype.Component

/**
 * OrderPaidEvent를 처리하여 재고 확정 차감 이벤트를 발행하는 핸들러
 */
@Component
class OrderPaidEventHandler(private val inventoryEventPublishPort: InventoryEventPublishPort) :
    DomainEventHandler<OrderPaidEvent> {

    override fun supports(event: DomainEvent): Boolean = event is OrderPaidEvent

    override fun handle(event: OrderPaidEvent) {
        // 재고 확정 차감 이벤트 발행
        val inventoryConfirmEvent = InventoryConfirmRequested(
            orderId = event.orderId,
            items = event.items.map {
                InventoryConfirmItem(
                    productId = it.productId,
                    quantity = it.quantity,
                )
            },
        )
        inventoryEventPublishPort.publishConfirmRequest(inventoryConfirmEvent)
    }
}
