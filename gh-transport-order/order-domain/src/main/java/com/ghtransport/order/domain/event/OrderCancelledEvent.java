package com.ghtransport.order.domain.event;

import com.ghtransport.common.domain.DomainEvent;
import lombok.Getter;

@Getter
public class OrderCancelledEvent extends DomainEvent {

    private final String orderId;
    private final String reason;

    public OrderCancelledEvent(String orderId, String reason) {
        super();
        this.orderId = orderId;
        this.reason = reason;
    }
}
