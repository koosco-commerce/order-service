package com.koosco.orderservice.order.infra.event

import com.koosco.common.core.event.CloudEvent
import com.koosco.orderservice.order.application.event.RefundRequested
import com.koosco.orderservice.order.application.port.outbound.RefundEventPublishPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class KafkaRefundEventPublishAdapter(
    private val kafkaTemplate: KafkaTemplate<String, CloudEvent<*>>,
    @Value("\${kafka.topics.refund-requested}")
    private val topic: String,
) : RefundEventPublishPort {

    private val logger = LoggerFactory.getLogger(KafkaRefundEventPublishAdapter::class.java)

    override fun publishRefundRequest(event: RefundRequested) {
        val cloudEvent = CloudEvent(
            id = UUID.randomUUID().toString(),
            source = "order-service",
            type = "refund.requested",
            dataContentType = "application/json",
            time = Instant.now(),
            data = event,
        )

        kafkaTemplate.send(topic, event.orderId.toString(), cloudEvent)
            .whenComplete { result, ex ->
                if (ex == null) {
                    logger.info("환불 요청 이벤트 발행 성공: orderId=${event.orderId}, topic=$topic")
                } else {
                    logger.error("환불 요청 이벤트 발행 실패: orderId=${event.orderId}", ex)
                }
            }
    }
}
