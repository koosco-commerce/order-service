package com.koosco.orderservice.order.infra.messaging.kafka

import com.koosco.common.core.event.DomainEvent
import com.koosco.inventoryservice.infra.messaging.DomainTopicResolver
import com.koosco.orderservice.order.application.contract.OrderIntegrationEvent
import com.koosco.orderservice.order.infra.messaging.IntegrationTopicResolver
import org.springframework.stereotype.Component

/**
 * fileName       : KafkaTopicResolver
 * author         : koo
 * date           : 2025. 12. 22. 오전 4:42
 * description    :
 */
@Component
class OrderDomainTopicResolver(private val topicProperties: KafkaTopicProperties) : DomainTopicResolver {
    override fun resolve(event: DomainEvent): String = topicProperties.mappings[event.getEventType()]
        ?: topicProperties.default
}

@Component
class OrderIntegrationTopicResolver(private val props: KafkaIntegrationProperties) : IntegrationTopicResolver {

    override fun resolve(event: OrderIntegrationEvent): String = props.mappings[event.getEventType()]
        ?: props.default
}
