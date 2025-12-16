package com.koosco.orderservice.order.application.port.outbound

import com.koosco.orderservice.order.application.event.OrderCreated

interface OrderCreateEventPublishPort {

    fun publish(event: OrderCreated)
}
