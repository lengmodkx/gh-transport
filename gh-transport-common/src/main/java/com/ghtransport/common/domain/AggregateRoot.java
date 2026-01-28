package com.ghtransport.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 */
public abstract class AggregateRoot<ID> extends Entity<ID> {

    @JsonIgnore
    private transient List<DomainEvent> domainEvents = new ArrayList<>();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
