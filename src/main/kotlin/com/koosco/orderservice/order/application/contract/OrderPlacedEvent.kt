package com.koosco.orderservice.order.application.contract

import com.koosco.orderservice.order.domain.Order
import com.koosco.orderservice.order.domain.event.OrderPlaced
import java.util.UUID

/**
 * fileName       : OrderContract
 * author         : koo
 * date           : 2025. 12. 23. 오전 2:50
 * description    :
 */

/**
 * 주문 생성 성공
 * [
 *      payment service : 결제 초기화
 *      inventory service : 재고 예약
 * ]
 */
data class OrderPlacedEvent(
    override val orderId: Long,
    val userId: Long,
    val payableAmount: Long,
    val items: List<Item>,

    val correlationId: String,
    val causationId: String? = null,
) : OrderIntegrationEvent {
    data class Item(val skuId: String, val quantity: Int, val unitPrice: Long)

    override fun getEventType(): String = "order.placed"

    companion object {

        fun from(order: Order): OrderPlacedEvent {
            val placedEvent = (
                order.pullDomainEvents()
                    .filterIsInstance<OrderPlaced>()
                    .singleOrNull()
                    ?: throw IllegalStateException()
                )

            return OrderPlacedEvent(
                orderId = placedEvent.orderId,
                userId = placedEvent.userId,
                payableAmount = order.payableAmount.amount,
                items = placedEvent.items.map {
                    Item(
                        skuId = it.skuId,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice,
                    )
                },

                correlationId = placedEvent.orderId.toString(),
                causationId = UUID.randomUUID().toString(),
            )
        }
    }
}
