package com.ghtransport.order.infrastructure.event;

import com.ghtransport.common.domain.DomainEvent;
import com.ghtransport.common.domain.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event: {}", event.getClass().getSimpleName());
        eventPublisher.publishEvent(event);
    }
}
