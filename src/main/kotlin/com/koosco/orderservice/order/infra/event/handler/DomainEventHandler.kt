package com.koosco.orderservice.order.infra.event.handler

import com.koosco.common.core.event.DomainEvent

/**
 * 도메인 이벤트를 처리하는 핸들러 인터페이스
 * 각 도메인 이벤트 타입에 대해 하나의 핸들러를 구현
 */
interface DomainEventHandler<T : DomainEvent> {
    /**
     * 이 핸들러가 처리할 수 있는 이벤트 타입
     */
    fun supports(event: DomainEvent): Boolean

    /**
     * 도메인 이벤트를 처리 (통합 이벤트 발행 등)
     */
    fun handle(event: T)
}
