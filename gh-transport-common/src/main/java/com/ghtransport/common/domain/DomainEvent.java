package com.ghtransport.common.domain;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 领域事件基类
 */
@Getter
public abstract class DomainEvent {

    private final String eventId;
    private final LocalDateTime occurredAt;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
    }
}
