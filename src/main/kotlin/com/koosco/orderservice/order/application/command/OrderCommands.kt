package com.koosco.orderservice.order.application.command

import com.koosco.orderservice.order.domain.vo.Money

data class CreateOrderCommand(
    val userId: Long,
    val items: List<OrderItemCommand>,
    val discountAmount: Money = Money(0L),
)

data class OrderItemCommand(val productId: Long, val quantity: Int, val unitPrice: Money)

data class ProcessPaymentCallbackCommand(
    val orderId: Long,
    val paymentId: String,
    val status: String,
    val paidAmount: Long,
)

data class RefundOrderItemsCommand(val orderId: Long, val refundItemIds: List<Long>)
