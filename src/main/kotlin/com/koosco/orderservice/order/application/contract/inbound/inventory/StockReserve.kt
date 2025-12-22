package com.koosco.orderservice.order.application.contract.inbound.inventory

/**
 * fileName       : StockReserve
 * author         : koo
 * date           : 2025. 12. 23. 오전 3:31
 * description    :
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
data class StockReserveFailedEvent(
    val orderId: Long,
    val reservationId: String? = null,
    val reason: String, // e.g. NOT_ENOUGH_STOCK
    val failedItems: List<Item>? = null,
    val occurredAt: Long,
) {
    data class Item(val skuId: String, val quantity: Int, val availableQuantity: Int? = null)
}
