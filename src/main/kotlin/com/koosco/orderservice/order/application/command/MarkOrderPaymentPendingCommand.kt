package com.koosco.orderservice.order.application.command

/**
 * fileName       : InventoryCommands
 * author         : koo
 * date           : 2025. 12. 23. 오전 12:29
 * description    :
 */
/**
 * 재고 확정 이후 결제 대기 상태로 변경
 */
data class MarkOrderPaymentPendingCommand(
    val orderId: Long,
    val reservationId: String,
    val items: List<Item> = emptyList(),
) {
    data class Item(val skuId: String, val quantity: Int)
}

data class MarkOrderConfirmedCommand(
    val orderId: Long,
    val reservationId: String? = null,
    val items: List<Item> = emptyList(),
) {
    data class Item(val skuId: String, val quantity: Int)
}
