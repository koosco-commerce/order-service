package com.koosco.orderservice.order.infra.event

import com.koosco.common.core.event.CloudEvent
import com.koosco.orderservice.order.application.event.InventoryConfirmRequested
import com.koosco.orderservice.order.application.event.InventoryReserveRequested
import com.koosco.orderservice.order.application.port.outbound.InventoryEventPublishPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class KafkaInventoryEventPublishAdapter(
    private val kafkaTemplate: KafkaTemplate<String, CloudEvent<*>>,
    @Value("\${kafka.topics.inventory-reserve-requested}")
    private val reserveTopic: String,
    @Value("\${kafka.topics.inventory-confirm-requested}")
    private val confirmTopic: String,
) : InventoryEventPublishPort {

    private val logger = LoggerFactory.getLogger(KafkaInventoryEventPublishAdapter::class.java)

    override fun publishReserveRequest(event: InventoryReserveRequested) {
        val cloudEvent = CloudEvent(
            id = UUID.randomUUID().toString(),
            source = "order-service",
            type = "inventory.reserve.requested",
            dataContentType = "application/json",
            time = Instant.now(),
            data = event,
        )

        kafkaTemplate.send(reserveTopic, event.orderId.toString(), cloudEvent)
            .whenComplete { result, ex ->
                if (ex == null) {
                    logger.info("재고 예약 요청 이벤트 발행 성공: orderId=${event.orderId}, topic=$reserveTopic")
                } else {
                    logger.error("재고 예약 요청 이벤트 발행 실패: orderId=${event.orderId}", ex)
                }
            }
    }

    override fun publishConfirmRequest(event: InventoryConfirmRequested) {
        val cloudEvent = CloudEvent(
            id = UUID.randomUUID().toString(),
            source = "order-service",
            type = "inventory.confirm.requested",
            dataContentType = "application/json",
            time = Instant.now(),
            data = event,
        )

        kafkaTemplate.send(confirmTopic, event.orderId.toString(), cloudEvent)
            .whenComplete { result, ex ->
                if (ex == null) {
                    logger.info("재고 확정 요청 이벤트 발행 성공: orderId=${event.orderId}, topic=$confirmTopic")
                } else {
                    logger.error("재고 확정 요청 이벤트 발행 실패: orderId=${event.orderId}", ex)
                }
            }
    }
}
