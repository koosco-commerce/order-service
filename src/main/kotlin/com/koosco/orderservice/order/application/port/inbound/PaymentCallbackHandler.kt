package com.koosco.orderservice.order.application.port.inbound

import com.koosco.orderservice.order.application.event.PaymentCompleted

interface PaymentCallbackHandler {

    fun handlePaymentCompleted(event: PaymentCompleted)
}
