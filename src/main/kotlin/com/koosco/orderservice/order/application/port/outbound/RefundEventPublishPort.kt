package com.koosco.orderservice.order.application.port.outbound

import com.koosco.orderservice.order.application.event.RefundRequested

interface RefundEventPublishPort {

    fun publishRefundRequest(event: RefundRequested)
}
