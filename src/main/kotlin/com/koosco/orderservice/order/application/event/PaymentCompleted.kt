package com.koosco.orderservice.order.application.event

data class PaymentCompleted(val orderId: Long, val paymentId: String, val paidAmount: Long, val status: String)
