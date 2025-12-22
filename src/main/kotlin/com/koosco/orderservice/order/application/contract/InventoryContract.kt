package com.koosco.orderservice.order.application.contract

import com.koosco.common.core.event.DomainEvent
import com.koosco.orderservice.common.MessageContext
import com.koosco.orderservice.order.domain.Order
import com.koosco.orderservice.order.domain.event.OrderCancelReason
import com.koosco.orderservice.order.domain.event.OrderCancelledEvent
import com.koosco.orderservice.order.domain.event.OrderPaidEvent

/**
 * fileName       : InventoryContract
 * author         : koo
 * date           : 2025. 12. 23. 오전 2:50
 * description    :
 */

/**
 * ================================
 * 재고 예약
 * ================================
 */
// 재고 예약 성공
data class StockReservedEvent(
    val orderId: Long,
    val reservationId: String,
    val items: List<Item>,

    val correlationId: String,
    val causationId: String?,
) {
    data class Item(val skuId: String, val quantity: Int)
}

// 재고 예약 실패
data class InventoryReserveFailedEvent(
    val orderId: Long,
    val reservationId: String? = null,
    val reason: String, // e.g. NOT_ENOUGH_STOCK
    val failedItems: List<Item>? = null,
    val occurredAt: Long,
) {
    data class Item(val skuId: String, val quantity: Int, val availableQuantity: Int? = null)
}

/**
 * ================================
 * 재고 확정
 * ================================
 */

// 재고 확정 요청
data class InventoryConfirmRequestedEvent(
    override val orderId: Long,
    val items: List<Item>,
    val correlationId: String,
    val causationId: String?,
) : OrderIntegrationEvent {

    data class Item(val skuId: String, val quantity: Int)

    override fun getEventType(): String = "inventory.confirm.requested"

    companion object {
        fun from(
            order: Order,
            domainEvents: List<DomainEvent>,
            context: MessageContext,
        ): InventoryConfirmRequestedEvent {
            val paid = domainEvents.filterIsInstance<OrderPaidEvent>().singleOrNull()
                ?: throw IllegalStateException("No OrderPaidEvent in this UoW")

            return InventoryConfirmRequestedEvent(
                orderId = paid.orderId,
                items = paid.items.map { Item(it.skuId, it.quantity) },
                correlationId = context.correlationId,
                causationId = context.causationId,
            )
        }
    }
}

// 재고 확정 성공
data class StockConfirmedEvent(
    val orderId: Long,
    val reservationId: String,
    val items: List<Item>,

    val correlationId: String,
    val causationId: String?,
) {
    data class Item(val skuId: String, val quantity: Int)
}

// 재고 확정 실패
data class StockConfirmFailedEvent(
    val orderId: Long,
    val reservationId: String? = null,
    val reason: String, // e.g. RESERVATION_NOT_FOUND, NOT_ENOUGH_STOCK, INTERNAL_ERROR
    val correlationId: String,
    val causationId: String? = null,
)

/**
 * ================================
 * 재고 예약 취소
 * ================================
 */

// 재고 예약 취소 요청
data class InventoryReleaseRequestedEvent(
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
        fun from(order: Order, context: MessageContext): InventoryReleaseRequestedEvent {
            val cancelled = order.pullDomainEvents().filterIsInstance<OrderCancelledEvent>().singleOrNull()
                ?: throw IllegalStateException("OrderCancelledEvent not created")

            return InventoryReleaseRequestedEvent(
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

// 응답 이벤트 없음
