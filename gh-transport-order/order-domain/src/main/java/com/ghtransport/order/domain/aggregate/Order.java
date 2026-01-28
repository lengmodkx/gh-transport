package com.ghtransport.order.domain.aggregate;

import com.ghtransport.common.domain.AggregateRoot;
import com.ghtransport.common.util.Validate;
import com.ghtransport.order.domain.entity.OrderItem;
import com.ghtransport.order.domain.event.OrderCancelledEvent;
import com.ghtransport.order.domain.event.OrderCreatedEvent;
import com.ghtransport.order.domain.vo.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Order extends AggregateRoot<String> {

    private OrderNo orderNo;
    private String customerId;
    private List<OrderItem> items;
    private Address pickupAddress;
    private Address deliveryAddress;
    private Money totalAmount;
    private OrderStatus status;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 工厂方法
    public static Order create(String customerId, List<OrderItem> items,
                                Address pickupAddress, Address deliveryAddress,
                                LocalDateTime pickupTime, LocalDateTime deliveryTime) {
        Validate.notBlank(customerId, "客户ID不能为空");
        Validate.isTrue(items != null && !items.isEmpty(), "货物明细不能为空");

        Order order = new Order();
        order.id = UUID.randomUUID().toString();
        order.orderNo = OrderNo.generate();
        order.customerId = customerId;
        order.items = new ArrayList<>(items);
        order.pickupAddress = pickupAddress;
        order.deliveryAddress = deliveryAddress;
        order.totalAmount = calculateTotal(items);
        order.status = OrderStatus.PENDING;
        order.pickupTime = pickupTime;
        order.deliveryTime = deliveryTime;
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();

        order.registerEvent(new OrderCreatedEvent(order.id, order.orderNo.getValue(), customerId));

        return order;
    }

    // 业务行为：取消订单
    public void cancel(String reason) {
        if (!status.isPending()) {
            throw new com.ghtransport.common.exception.DomainException("只能取消待调度状态的订单");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        this.registerEvent(new OrderCancelledEvent(this.id, reason));
    }

    // 业务行为：标记已调度
    public void markDispatched() {
        status.validateTransition(OrderStatus.DISPATCHED);
        this.status = OrderStatus.DISPATCHED;
        this.updatedAt = LocalDateTime.now();
    }

    // 业务行为：开始运输
    public void startTransport() {
        status.validateTransition(OrderStatus.IN_TRANSIT);
        this.status = OrderStatus.IN_TRANSIT;
        this.updatedAt = LocalDateTime.now();
    }

    // 业务行为：标记送达
    public void markDelivered() {
        status.validateTransition(OrderStatus.DELIVERED);
        this.status = OrderStatus.DELIVERED;
        this.updatedAt = LocalDateTime.now();
    }

    // 业务行为：完成订单
    public void complete() {
        status.validateTransition(OrderStatus.COMPLETED);
        this.status = OrderStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    protected String generateId() {
        return UUID.randomUUID().toString();
    }

    private static Money calculateTotal(List<OrderItem> items) {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(Money.ZERO, Money::add);
    }
}
