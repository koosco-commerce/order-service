package com.koosco.orderservice.order.infra.messaging.kafka.consumer

import com.koosco.common.core.event.CloudEvent
import com.koosco.orderservice.common.MessageContext
import com.koosco.orderservice.order.application.command.MarkOrderPaymentPendingCommand
import com.koosco.orderservice.order.application.contract.inbound.inventory.StockReservedEvent
import com.koosco.orderservice.order.application.usecase.MarkOrderPaymentPendingUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * fileName       : KafkaStockReservedConsumer
 * author         : koo
 * date           : 2025. 12. 22. 오전 6:18
 * description    :
 */
@Component
class KafkaStockReservedConsumer(private val markOrderPaymentPendingUseCase: MarkOrderPaymentPendingUseCase) {
    private val logger = LoggerFactory.getLogger(KafkaPaymentCompletedConsumer::class.java)

    @KafkaListener(
        topics = ["\${order.topic.mappings.stock.reserved}"],
        groupId = "\${spring.kafka.consumer.group-id:order-service-group}",
    )
    fun onStockReserved(event: CloudEvent<StockReservedEvent>, ack: Acknowledgment) {
        val data = event.data
            ?: run {
                logger.error("StockReservedEvent is null: eventId=${event.id}")
                ack.acknowledge()
                return
            }

        val context = MessageContext(
            correlationId = data.correlationId,
            causationId = event.id,
        )

        markOrderPaymentPendingUseCase.execute(
            MarkOrderPaymentPendingCommand(
                orderId = data.orderId,
                reservationId = data.reservationId,
            ),
        )

        ack.acknowledge()
    }
}
