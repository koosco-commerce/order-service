package com.koosco.orderservice.order.infra.messaging.kafka.consumer

import com.koosco.common.core.event.CloudEvent
import com.koosco.orderservice.common.MessageContext
import com.koosco.orderservice.order.application.command.CancelOrderCommand
import com.koosco.orderservice.order.application.contract.inbound.payment.PaymentCancelledEvent
import com.koosco.orderservice.order.application.usecase.CancelOrderByPaymentFailureUseCase
import com.koosco.orderservice.order.domain.enums.OrderCancelReason
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * fileName       : KafkaPaymentFailedConsumer
 * author         : koo
 * date           : 2025. 12. 23. 오전 2:45
 * description    :
 */
@Component
class KafkaPaymentFailedConsumer(private val cancelOrderByPaymentFailureUseCase: CancelOrderByPaymentFailureUseCase) {

    private val logger = LoggerFactory.getLogger(KafkaPaymentCompletedConsumer::class.java)

    @KafkaListener(
        topics = ["\${order.topic.integration.mappings.payment.completed}"],
        groupId = "\${spring.kafka.consumer.group-id:order-service-group}",
    )
    fun onPaymentFailed(event: CloudEvent<PaymentCancelledEvent>, ack: Acknowledgment) {
        val data = event.data
            ?: run {
                logger.error("PaymentCancelledEvent is null: eventId=${event.id}")
                ack.acknowledge()
                return
            }

        logger.info(
            "Received PaymentCancelledEvent: eventId=${event.id}, " +
                "orderId=${data.orderId}, paymentId=...",
        )

        val context = MessageContext(
            correlationId = data.correlationId,
            causationId = event.id,
        )

        cancelOrderByPaymentFailureUseCase.execute(
            CancelOrderCommand(
                orderId = data.orderId,
                reason = OrderCancelReason.valueOf(data.reason),
            ),
            context,
        )

        ack.acknowledge()
    }
}
