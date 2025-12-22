package com.koosco.orderservice.order.infra.messaging.kafka.consumer

import com.koosco.common.core.event.CloudEvent
import com.koosco.orderservice.common.MessageContext
import com.koosco.orderservice.order.application.command.MarkOrderConfirmedCommand
import com.koosco.orderservice.order.application.contract.inbound.inventory.StockConfirmedEvent
import com.koosco.orderservice.order.application.usecase.MarkOrderConfirmedUseCase
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * fileName       : KafkaStockConfirmedConsumer
 * author         : koo
 * date           : 2025. 12. 23. 오전 12:51
 * description    :
 */
@Component
class KafkaStockConfirmedConsumer(private val markOrderConfirmedUseCase: MarkOrderConfirmedUseCase) {
    private val logger = LoggerFactory.getLogger(KafkaPaymentCompletedConsumer::class.java)

    @KafkaListener(
        topics = ["\${order.topic.integration.mappings.stock.confirmed}"],
        groupId = "\${spring.kafka.consumer.group-id:order-service-group}",
    )
    fun onStockConfirmed(event: CloudEvent<StockConfirmedEvent>, ack: Acknowledgment) {
        val data = event.data
            ?: run {
                logger.error("StockConfirmedEvent is null: eventId=${event.id}")
                ack.acknowledge()
                return
            }

        val context = MessageContext(
            correlationId = data.correlationId,
            causationId = event.id,
        )

        markOrderConfirmedUseCase.execute(
            MarkOrderConfirmedCommand(
                orderId = data.orderId,
                reservationId = data.reservationId,
                items = data.items.map { MarkOrderConfirmedCommand.Item(it.skuId, it.quantity) },
            ),
        )
    }
}
