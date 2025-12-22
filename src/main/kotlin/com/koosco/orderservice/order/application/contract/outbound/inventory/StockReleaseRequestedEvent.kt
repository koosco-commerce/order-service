package com.koosco.orderservice.order.application.contract.outbound.inventory

import com.koosco.orderservice.common.MessageContext
import com.koosco.orderservice.order.application.contract.OrderIntegrationEvent
import com.koosco.orderservice.order.domain.Order
import com.koosco.orderservice.order.domain.event.OrderCancelReason
import com.koosco.orderservice.order.domain.event.OrderCancelledEvent

/**
 * fileName       : StockReleaseRequestedEvent
 * author         : koo
 * date           : 2025. 12. 23. 오전 3:30
 * description    :
 */
data class StockReleaseRequestedEvent(
    override val orderId: Long,
    val reason: OrderCancelReason,
    val items: List<Item>,
    val correlationId: String,
    val causationId: String? = null,
) : OrderIntegrationEvent {
    data class Item(val skuId: String, val quantity: Int)

    override fun getEventType(): String = "inventory.release.requested"
    override fun getPartitionKey(): String = orderId.toString()

    companion object {
        fun from(order: Order, context: MessageContext): StockReleaseRequestedEvent {
            val cancelled = order.pullDomainEvents().filterIsInstance<OrderCancelledEvent>().singleOrNull()
                ?: throw IllegalStateException("OrderCancelledEvent not created")

            return StockReleaseRequestedEvent(
                orderId = cancelled.orderId,
                reason = cancelled.reason,
                items = cancelled.items.map {
                    Item(
                        it.skuId,
                        it.quantity,
                    )
                },
                correlationId = cancelled.orderId.toString(),
                causationId = context.causationId,
            )
        }
    }
}
