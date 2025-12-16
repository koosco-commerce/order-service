package com.koosco.orderservice.order.application.event

data class OrderCreated(val orderId: Long, val reservedAmount: Long)
