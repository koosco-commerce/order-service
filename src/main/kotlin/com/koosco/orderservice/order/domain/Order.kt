package com.koosco.orderservice.order.domain

import com.koosco.common.core.event.DomainEvent
import com.koosco.orderservice.order.domain.event.OrderItemInfo
import com.koosco.orderservice.order.domain.event.OrderItemsRefundedEvent
import com.koosco.orderservice.order.domain.event.OrderPaidEvent
import com.koosco.orderservice.order.domain.event.RefundedItemInfo
import com.koosco.orderservice.order.domain.vo.Money
import com.koosco.orderservice.order.domain.vo.OrderAmount
import com.koosco.orderservice.order.domain.vo.OrderItemSpec
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Transient
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus,

    /** 주문 원금 (아이템 합계) */
    @Column(nullable = false)
    val totalAmount: Money,

    /** 쿠폰으로 할인된 총 금액 */
    @Column(nullable = false)
    val discountAmount: Money,

    /** 실제 결제 요청 금액 */
    @Column(nullable = false)
    val payableAmount: Money,

    /** 누적 환불 금액 */
    @Column(nullable = false)
    var refundedAmount: Money = Money(0L),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(
        mappedBy = "order",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    val items: MutableList<OrderItem> = mutableListOf(),
) {

    @Transient
    private val domainEvents: MutableList<DomainEvent> = mutableListOf()

    companion object {
        fun create(userId: Long, itemSpecs: List<OrderItemSpec>, amount: OrderAmount): Order {
            val order = Order(
                userId = userId,
                status = OrderStatus.INIT,
                totalAmount = amount.total,
                discountAmount = amount.discount,
                payableAmount = amount.payable,
                items = mutableListOf(),
            )

            itemSpecs.forEach { spec ->
                OrderItem.create(order, spec, amount).also {
                    order.items.add(it)
                }
            }

            return order
        }
    }

    fun pullDomainEvents(): List<DomainEvent> = domainEvents.toList().also { domainEvents.clear() }

    fun place() {
        require(status == OrderStatus.INIT) { "주문은 INIT 상태에서만 생성 가능합니다." }

        status = OrderStatus.CREATED
        updatedAt = LocalDateTime.now()
    }

    fun markReserved() {
        require(status == OrderStatus.CREATED) { "재고 예약은 CREATED 상태에서만 가능합니다." }
        status = OrderStatus.RESERVED
        updatedAt = LocalDateTime.now()
    }

    fun markPaid() {
        require(status == OrderStatus.PAYMENT_PENDING) { "결제 완료는 PAYMENT_PENDING 상태에서만 가능합니다." }
        status = OrderStatus.PAID
        updatedAt = LocalDateTime.now()

        domainEvents.add(
            OrderPaidEvent(
                orderId = id!!,
                paidAmount = payableAmount.amount,
                items = items.map {
                    OrderItemInfo(
                        productId = it.productId,
                        quantity = it.quantity,
                        unitPrice = it.unitPrice.amount,
                    )
                },
            ),
        )
    }

    fun markConfirmed() {
        require(status == OrderStatus.PAID) { "재고 확정은 PAID 상태에서만 가능합니다." }
        status = OrderStatus.CONFIRMED
        updatedAt = LocalDateTime.now()
    }

    /**
     * ==== REFUND ====
     */
    fun refundAll(itemIds: List<Long>): Money {
        val totalRefundAmount = itemIds.fold(Money.ZERO) { acc, itemId ->
            val item = items.first { it.id == itemId }
            acc + item.refund()
        }

        itemIds.forEach { itemId ->
            refundItem(itemId)
        }

        return totalRefundAmount
    }

    fun refundItem(itemId: Long): Money {
        if (!canRefund()) {
            throw IllegalArgumentException("환불 가능한 상태가 아닙니다. 현재 상태: $status")
        }

        val item = items.first { it.id == itemId }
        val refundAmount = item.refund()

        refundedAmount = refundedAmount + refundAmount
        updatedAt = LocalDateTime.now()

        status = if (isFullyRefunded()) {
            OrderStatus.REFUNDED
        } else {
            OrderStatus.PARTIALLY_REFUNDED
        }

        domainEvents.add(
            OrderItemsRefundedEvent(
                orderId = id!!,
                refundedAmount = refundAmount.amount,
                refundedItems = listOf(
                    RefundedItemInfo(
                        productId = item.productId,
                        quantity = item.quantity,
                        refundAmount = refundAmount.amount,
                    ),
                ),
            ),
        )

        return refundAmount
    }

    private fun canRefund(): Boolean = status == OrderStatus.CONFIRMED || status == OrderStatus.PARTIALLY_REFUNDED

    private fun isFullyRefunded(): Boolean = items.all { it.status == OrderItemStatus.REFUNDED }
}
