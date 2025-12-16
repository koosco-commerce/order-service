package com.koosco.orderservice.order.application.event

data class InventoryReserveRequested(val orderId: Long, val items: List<InventoryReserveItem>)

data class InventoryReserveItem(val productId: Long, val quantity: Int)
