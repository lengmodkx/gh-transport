package com.ghtransport.order.application.event;

import com.ghtransport.order.domain.event.OrderCancelledEvent;
import com.ghtransport.order.domain.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Order created: orderId={}, orderNo={}, customerId={}",
            event.getOrderId(), event.getOrderNo(), event.getCustomerId());
        // 这里可以触发其他业务逻辑，如发送通知等
    }

    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Order cancelled: orderId={}, reason={}",
            event.getOrderId(), event.getReason());
    }
}
