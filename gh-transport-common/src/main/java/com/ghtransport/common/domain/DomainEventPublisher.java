package com.ghtransport.common.domain;

/**
 * 领域事件发布器
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
