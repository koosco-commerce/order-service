package com.koosco.orderservice.order.infra.event

import com.koosco.common.core.event.DomainEvent
import com.koosco.orderservice.order.application.port.outbound.DomainEventPublishPort
import com.koosco.orderservice.order.infra.event.handler.DomainEventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 도메인 이벤트를 적절한 핸들러로 라우팅하는 어댑터
 *
 * Strategy 패턴을 사용하여 각 이벤트 타입별 핸들러를 등록하고,
 * 이벤트 발행 시 해당 핸들러를 찾아 처리를 위임합니다.
 *
 * 새로운 이벤트 타입 추가 시:
 * 1. DomainEventHandler 인터페이스를 구현하는 새 핸들러 생성
 * 2. @Component로 Spring Bean 등록
 * 3. 자동으로 핸들러 목록에 추가됨 (추가 코드 수정 불필요)
 */
@Component
class DomainEventPublishAdapter(private val handlers: List<DomainEventHandler<DomainEvent>>) : DomainEventPublishPort {

    private val logger = LoggerFactory.getLogger(DomainEventPublishAdapter::class.java)

    override fun publish(event: DomainEvent) {
        val handler = handlers.firstOrNull { it.supports(event) }

        if (handler != null) {
            logger.debug("도메인 이벤트 처리: eventType=${event.getEventType()}, aggregateId=${event.getAggregateId()}")
            handler.handle(event)
        } else {
            logger.warn("도메인 이벤트 핸들러를 찾을 수 없음: eventType=${event.getEventType()}, event=$event")
        }
    }

    override fun publishAll(events: List<DomainEvent>) {
        events.forEach { publish(it) }
    }
}
