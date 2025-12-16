package com.koosco.orderservice.order.application.port.outbound

import com.koosco.orderservice.order.application.event.InventoryConfirmRequested
import com.koosco.orderservice.order.application.event.InventoryReserveRequested

interface InventoryEventPublishPort {

    fun publishReserveRequest(event: InventoryReserveRequested)

    fun publishConfirmRequest(event: InventoryConfirmRequested)
}
