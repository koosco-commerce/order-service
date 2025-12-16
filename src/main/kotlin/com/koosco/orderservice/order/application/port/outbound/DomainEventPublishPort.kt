package com.koosco.orderservice.order.application.port.outbound

import com.koosco.common.core.event.DomainEvent

interface DomainEventPublishPort {

    fun publish(event: DomainEvent)

    fun publishAll(events: List<DomainEvent>)
}
