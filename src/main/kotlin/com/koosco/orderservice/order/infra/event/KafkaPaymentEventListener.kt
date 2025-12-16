package com.koosco.orderservice.order.infra.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.koosco.common.core.event.CloudEvent
import com.koosco.orderservice.order.application.event.PaymentCompleted
import com.koosco.orderservice.order.application.port.inbound.PaymentCallbackHandler
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * 결제 완료 이벤트 핸들러
 */
@Component
class KafkaPaymentEventListener(
    private val paymentCallbackHandler: PaymentCallbackHandler,
    private val objectMapper: ObjectMapper,
) {

    private val logger = LoggerFactory.getLogger(KafkaPaymentEventListener::class.java)

    @KafkaListener(
        topics = ["\${kafka.topics.payment-completed}"],
        groupId = "\${spring.kafka.consumer.group-id:order-service-group}",
    )
    fun handlePaymentCompleted(cloudEvent: CloudEvent<*>) {
        try {
            logger.info("결제 완료 이벤트 수신: eventId=${cloudEvent.id}, type=${cloudEvent.type}")

            // CloudEvent의 data를 PaymentCompleted로 변환
            val paymentEvent = objectMapper.convertValue(cloudEvent.data, PaymentCompleted::class.java)

            paymentCallbackHandler.handlePaymentCompleted(paymentEvent)

            logger.info("결제 완료 이벤트 처리 완료: orderId=${paymentEvent.orderId}")
        } catch (e: Exception) {
            logger.error("결제 완료 이벤트 처리 실패: eventId=${cloudEvent.id}", e)
            // TODO: Dead Letter Queue로 보내거나 재처리 로직 추가
            throw e
        }
    }
}
