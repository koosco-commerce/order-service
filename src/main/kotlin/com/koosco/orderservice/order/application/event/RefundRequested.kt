package com.koosco.orderservice.order.application.event

data class RefundRequested(val orderId: Long, val refundAmount: Long, val items: List<RefundItem>)

data class RefundItem(val productId: Long, val quantity: Int, val refundAmount: Long)
