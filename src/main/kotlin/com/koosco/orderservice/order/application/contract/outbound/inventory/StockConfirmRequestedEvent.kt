package com.koosco.orderservice.order.application.contract.outbound.inventory

import com.koosco.common.core.event.DomainEvent
import com.koosco.orderservice.common.MessageContext
import com.koosco.orderservice.order.application.contract.OrderIntegrationEvent
import com.koosco.orderservice.order.domain.Order
import com.koosco.orderservice.order.domain.event.OrderPaidEvent

/**
 * fileName       : StockConfirmRequestedEvent
 * author         : koo
 * date           : 2025. 12. 23. 오전 3:29
 * description    : 재고 확정 요청
 */
data class StockConfirmRequestedEvent(
    override val orderId: Long,
    val items: List<Item>,
    val correlationId: String,
    val causationId: String?,
) : OrderIntegrationEvent {

    data class Item(val skuId: String, val quantity: Int)

    override fun getEventType(): String = "inventory.confirm.requested"

    companion object {
        fun from(order: Order, domainEvents: List<DomainEvent>, context: MessageContext): StockConfirmRequestedEvent {
            val paid = domainEvents.filterIsInstance<OrderPaidEvent>().singleOrNull()
                ?: throw IllegalStateException("No OrderPaidEvent in this UoW")

            return StockConfirmRequestedEvent(
                orderId = paid.orderId,
                items = paid.items.map { Item(it.skuId, it.quantity) },
                correlationId = context.correlationId,
                causationId = context.causationId,
            )
        }
    }
}
