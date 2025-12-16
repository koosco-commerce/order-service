package com.koosco.orderservice.order.application.event

data class InventoryConfirmRequested(val orderId: Long, val items: List<InventoryConfirmItem>)

data class InventoryConfirmItem(val productId: Long, val quantity: Int)
