package com.koosco.orderservice.order.infra.messaging.kafka.consumer

import com.koosco.common.core.event.CloudEvent
import com.koosco.orderservice.common.MessageContext
import com.koosco.orderservice.order.application.command.MarkOrderPaidCommand
import com.koosco.orderservice.order.application.contract.inbound.payment.PaymentCompletedEvent
import com.koosco.orderservice.order.application.usecase.MarkOrderPaidUseCase
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

/**
 * 결제 완료 이벤트 핸들러
 */
@Component
@Validated
class KafkaPaymentCompletedConsumer(private val markOrderPaidUseCase: MarkOrderPaidUseCase) {

    private val logger = LoggerFactory.getLogger(KafkaPaymentCompletedConsumer::class.java)

    @KafkaListener(
        topics = ["\${order.topic.mappings.payment.failed}"],
        groupId = "\${spring.kafka.consumer.group-id:order-service-group}",
    )
    fun onPaymentCompleted(@Valid event: CloudEvent<PaymentCompletedEvent>, ack: Acknowledgment) {
        val data = event.data
            ?: run {
                logger.error("PaymentCompleted is null: eventId=${event.id}")
                ack.acknowledge()
                return
            }

        logger.info(
            "Received PaymentCompleted: eventId=${event.id}, " +
                "orderId=${data.orderId}, paymentId=...",
        )

        val context = MessageContext(
            correlationId = data.correlationId,
            causationId = event.id,
        )

        try {
            // 주문 확정
            markOrderPaidUseCase.execute(
                MarkOrderPaidCommand(
                    orderId = data.orderId,
                    paidAmount = data.paidAmount,
                ),
                context,
            )

            ack.acknowledge()

            logger.info(
                "Successfully confirm order for Order: eventId=${event.id}, orderId=${data.orderId}...",
            )
        } catch (e: Exception) {
            logger.error(
                "Failed to process Payment Completed event: ${event.id}, orderId=${data.orderId}",
                e,
            )
        }
        // 일단 바로 환불 진행하도록 진행
        // TODO : 실패시 재처리 시도 후 환불 수행
    }
}
