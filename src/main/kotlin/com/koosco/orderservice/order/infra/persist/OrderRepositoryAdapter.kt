package com.koosco.orderservice.order.infra.persist

import com.koosco.orderservice.order.application.port.outbound.OrderRepositoryPort
import com.koosco.orderservice.order.domain.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class OrderRepositoryAdapter(private val orderRepository: OrderRepository) : OrderRepositoryPort {

    override fun save(order: Order): Order = orderRepository.save(order)

    override fun findById(orderId: Long): Order? = orderRepository.findById(orderId).orElse(null)

    override fun findByUserId(userId: Long): List<Order> = orderRepository.findByUserId(userId)

    override fun findByUserId(userId: Long, pageable: Pageable): Page<Order> =
        orderRepository.findByUserId(userId, pageable)
}
