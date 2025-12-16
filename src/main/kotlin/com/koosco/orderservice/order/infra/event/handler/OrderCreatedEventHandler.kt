package com.koosco.orderservice.order.infra.event.handler

import com.koosco.common.core.event.DomainEvent
import com.koosco.orderservice.order.application.event.InventoryReserveItem
import com.koosco.orderservice.order.application.event.InventoryReserveRequested
import com.koosco.orderservice.order.application.port.outbound.InventoryEventPublishPort
import com.koosco.orderservice.order.domain.event.OrderCreatedEvent
import org.springframework.stereotype.Component

/**
 * OrderCreatedEvent를 처리하여 재고 예약 요청 이벤트를 발행하는 핸들러
 */
@Component
class OrderCreatedEventHandler(private val inventoryEventPublishPort: InventoryEventPublishPort) :
    DomainEventHandler<OrderCreatedEvent> {

    override fun supports(event: DomainEvent): Boolean = event is OrderCreatedEvent

    override fun handle(event: OrderCreatedEvent) {
        // 재고 예약 요청 이벤트 발행
        val inventoryReserveEvent = InventoryReserveRequested(
            orderId = event.orderId,
            items = event.items.map {
                InventoryReserveItem(
                    productId = it.productId,
                    quantity = it.quantity,
                )
            },
        )
        inventoryEventPublishPort.publishReserveRequest(inventoryReserveEvent)
    }
}
