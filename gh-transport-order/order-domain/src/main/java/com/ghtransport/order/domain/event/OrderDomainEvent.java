package com.ghtransport.order.domain.event;

import com.ghtransport.common.core.ddd.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单创建事件
 */
@Getter
@Setter
public class OrderCreatedEvent extends DomainEvent {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 客户ID
     */
    private String customerId;

    public OrderCreatedEvent(String orderId, String orderNo, String customerId) {
        super(orderId);
        this.orderNo = orderNo;
        this.customerId = customerId;
    }

    @Override
    public String getBusinessDescription() {
        return String.format("订单已创建: %s, 客户: %s", orderNo, customerId);
    }
}

/**
 * 订单确认事件
 */
@Getter
@Setter
class OrderConfirmedEvent extends DomainEvent {

    private String orderNo;

    public OrderConfirmedEvent(String orderId, String orderNo) {
        super(orderId);
        this.orderNo = orderNo;
    }

    @Override
    public String getBusinessDescription() {
        return "订单已确认: " + orderNo;
    }
}

/**
 * 订单取消事件
 */
@Getter
@Setter
class OrderCancelledEvent extends DomainEvent {

    private String orderNo;
    private String cancelReason;

    public OrderCancelledEvent(String orderId, String orderNo, String cancelReason) {
        super(orderId);
        this.orderNo = orderNo;
        this.cancelReason = cancelReason;
    }

    @Override
    public String getBusinessDescription() {
        return String.format("订单已取消: %s, 原因: %s", orderNo, cancelReason);
    }
}

/**
 * 订单发货事件
 */
@Getter
@Setter
class OrderShippedEvent extends DomainEvent {

    private String orderNo;
    private String trackingNo;

    public OrderShippedEvent(String orderId, String orderNo, String trackingNo) {
        super(orderId);
        this.orderNo = orderNo;
        this.trackingNo = trackingNo;
    }

    @Override
    public String getBusinessDescription() {
        return String.format("订单已发货: %s, 运单号: %s", orderNo, trackingNo);
    }
}

/**
 * 订单完成事件
 */
@Getter
@Setter
class OrderCompletedEvent extends DomainEvent {

    private String orderNo;

    public OrderCompletedEvent(String orderId, String orderNo) {
        super(orderId);
        this.orderNo = orderNo;
    }

    @Override
    public String getBusinessDescription() {
        return "订单已完成: " + orderNo;
    }
}
