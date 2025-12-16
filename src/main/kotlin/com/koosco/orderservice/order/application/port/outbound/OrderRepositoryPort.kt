package com.koosco.orderservice.order.application.port.outbound

import com.koosco.orderservice.order.domain.Order
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface OrderRepositoryPort {

    fun save(order: Order): Order

    fun findById(orderId: Long): Order?

    fun findByUserId(userId: Long): List<Order>

    fun findByUserId(userId: Long, pageable: Pageable): Page<Order>
}
