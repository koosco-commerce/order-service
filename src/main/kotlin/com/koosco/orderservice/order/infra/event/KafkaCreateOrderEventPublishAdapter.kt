package com.koosco.orderservice.order.infra.event

import com.koosco.common.core.event.CloudEvent
import com.koosco.orderservice.order.application.event.OrderCreated
import com.koosco.orderservice.order.application.port.outbound.OrderCreateEventPublishPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class KafkaCreateOrderEventPublishAdapter(
    private val kafkaTemplate: KafkaTemplate<String, CloudEvent<*>>,
    @Value("\${kafka.topics.order-created}")
    private val topic: String,
) : OrderCreateEventPublishPort {

    private val logger = LoggerFactory.getLogger(KafkaCreateOrderEventPublishAdapter::class.java)

    override fun publish(event: OrderCreated) {
        val cloudEvent = CloudEvent(
            id = UUID.randomUUID().toString(),
            source = "order-service",
            type = "order.created",
            dataContentType = "application/json",
            time = Instant.now(),
            data = event,
        )

        kafkaTemplate.send(topic, event.orderId.toString(), cloudEvent)
            .whenComplete { result, ex ->
                if (ex == null) {
                    logger.info("주문 생성 이벤트 발행 성공: orderId=${event.orderId}, topic=$topic")
                } else {
                    logger.error("주문 생성 이벤트 발행 실패: orderId=${event.orderId}", ex)
                }
            }
    }
}
