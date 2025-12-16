package com.koosco.orderservice.order.application.usecase

import com.koosco.common.core.annotation.UseCase
import com.koosco.orderservice.order.application.command.CreateOrderCommand
import com.koosco.orderservice.order.application.port.outbound.DomainEventPublishPort
import com.koosco.orderservice.order.application.port.outbound.OrderRepositoryPort
import com.koosco.orderservice.order.application.result.CreateOrderResult
import com.koosco.orderservice.order.domain.Order
import com.koosco.orderservice.order.domain.vo.OrderAmount
import com.koosco.orderservice.order.domain.vo.OrderItemSpec
import org.springframework.transaction.annotation.Transactional

@UseCase
class CreateOrderUseCase(
    private val orderRepository: OrderRepositoryPort,
    private val domainEventPublishPort: DomainEventPublishPort,
) {

    @Transactional
    fun execute(command: CreateOrderCommand): CreateOrderResult {
        // item spec 생성
        val itemSpecs = command.items.map {
            OrderItemSpec(
                productId = it.productId,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
            )
        }

        // OrderAmount 계산
        val orderAmount = OrderAmount.from(
            itemSpecs = itemSpecs,
            discount = command.discountAmount,
        )

        // 주문 생성
        val order = Order.create(
            userId = command.userId,
            itemSpecs = itemSpecs,
            amount = orderAmount,
        )

        val savedOrder = orderRepository.save(order)
        savedOrder.place()

        domainEventPublishPort.publishAll(savedOrder.pullDomainEvents())

        return CreateOrderResult(
            orderId = savedOrder.id!!,
            status = savedOrder.status,
            payableAmount = savedOrder.payableAmount.amount,
        )
    }
}
