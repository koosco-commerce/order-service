package com.koosco.orderservice.order.application.usecase

import com.koosco.common.core.annotation.UseCase
import com.koosco.common.core.exception.BadRequestException
import com.koosco.common.core.exception.ConflictException
import com.koosco.common.core.exception.NotFoundException
import com.koosco.orderservice.common.error.OrderErrorCode
import com.koosco.orderservice.order.application.event.PaymentCompleted
import com.koosco.orderservice.order.application.port.inbound.PaymentCallbackHandler
import com.koosco.orderservice.order.application.port.outbound.DomainEventPublishPort
import com.koosco.orderservice.order.application.port.outbound.OrderRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional

@UseCase
class HandlePaymentCallbackUseCase(
    private val orderRepository: OrderRepositoryPort,
    private val domainEventPublishPort: DomainEventPublishPort,
) : PaymentCallbackHandler {

    private val logger = LoggerFactory.getLogger(HandlePaymentCallbackUseCase::class.java)

    @Transactional
    override fun handlePaymentCompleted(event: PaymentCompleted) {
        logger.info("결제 완료 이벤트 수신: orderId=${event.orderId}, paymentId=${event.paymentId}")

        val order = orderRepository.findById(event.orderId)
            ?: throw NotFoundException(
                OrderErrorCode.ORDER_NOT_FOUND,
                "주문을 찾을 수 없습니다. orderId: ${event.orderId}",
            )

        // 결제 상태 확인
        when (event.status.uppercase()) {
            "SUCCESS", "PAID", "COMPLETED" -> {
                // 결제 금액 검증
                if (event.paidAmount != order.payableAmount.amount) {
                    throw ConflictException(
                        OrderErrorCode.PAYMENT_AMOUNT_MISMATCH,
                        "결제 금액이 일치하지 않습니다. expected: ${order.payableAmount.amount}, actual: ${event.paidAmount}",
                    )
                }

                // 도메인 이벤트 수집 및 발행
                order.markPaid()
                val domainEvents = order.pullDomainEvents()
                orderRepository.save(order)

                domainEventPublishPort.publishAll(domainEvents)

                logger.info("주문 상태 변경 완료: orderId=${order.id}, status=${order.status}")
            }

            "FAILED", "CANCELLED" -> {
                // TODO: 주문 실패 처리 및 재고 예약 해제
                logger.warn("결제 실패 처리 필요: orderId=${event.orderId}, status=${event.status}")
            }

            else -> {
                throw BadRequestException(
                    OrderErrorCode.INVALID_PAYMENT_STATUS,
                    "알 수 없는 결제 상태입니다. status: ${event.status}",
                )
            }
        }
    }
}
