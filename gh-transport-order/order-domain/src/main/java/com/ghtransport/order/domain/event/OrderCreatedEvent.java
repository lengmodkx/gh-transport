package com.ghtransport.order.domain.event;

import com.ghtransport.common.domain.DomainEvent;
import lombok.Getter;

@Getter
public class OrderCreatedEvent extends DomainEvent {

    private final String orderId;
    private final String orderNo;
    private final String customerId;

    public OrderCreatedEvent(String orderId, String orderNo, String customerId) {
        super();
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.customerId = customerId;
    }
}
