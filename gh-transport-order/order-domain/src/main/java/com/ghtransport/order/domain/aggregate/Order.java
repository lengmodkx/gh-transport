package com.ghtransport.order.domain.aggregate;

import com.ghtransport.common.core.ddd.AggregateRoot;
import com.ghtransport.common.core.ddd.DomainEvent;
import com.ghtransport.common.core.ddd.ValueObject;
import com.ghtransport.common.core.exception.BusinessException;
import com.ghtransport.common.core.result.ResultCode;
import com.ghtransport.common.core.util.IdGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单聚合根
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class Order extends AggregateRoot<OrderId> {

    /**
     * 订单号
     */
    private OrderNo orderNo;

    /**
     * 客户ID
     */
    private String customerId;

    /**
     * 收货地址
     */
    private Address shippingAddress;

    /**
     * 订单状态
     */
    private OrderStatus status;

    /**
     * 订单金额
     */
    private Money totalAmount;

    /**
     * 订单明细
     */
    private List<OrderItem> items;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    protected Order() {
        super();
    }

    protected Order(OrderId id) {
        super(id);
    }

    /**
     * 创建订单
     */
    public static Order create(String customerId, Address shippingAddress, List<OrderItem> items, String remark) {
        Order order = new Order(OrderId.generate());
        order.orderNo = OrderNo.generate();
        order.customerId = customerId;
        order.shippingAddress = shippingAddress;
        order.items = new ArrayList<>(items);
        order.status = OrderStatus.PENDING;
        order.remark = remark;
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();
        order.calculateTotalAmount();
        return order;
    }

    /**
     * 计算总金额
     */
    private void calculateTotalAmount() {
        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = Money.of(total);
    }

    /**
     * 确认订单
     */
    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new BusinessException("ORDER_STATUS_ERROR", "只有待确认的订单可以确认");
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
        registerEvent(new OrderConfirmedEvent(this.id.getValue(), this.orderNo.getValue()));
    }

    /**
     * 取消订单
     */
    public void cancel(String reason) {
        if (status != OrderStatus.PENDING && status != OrderStatus.CONFIRMED) {
            throw new BusinessException("ORDER_STATUS_ERROR", "当前状态不允许取消订单");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
        registerEvent(new OrderCancelledEvent(this.id.getValue(), this.orderNo.getValue(), reason));
    }

    /**
     * 发货
     */
    public void ship(String trackingNo) {
        if (status != OrderStatus.CONFIRMED) {
            throw new BusinessException("ORDER_STATUS_ERROR", "只有已确认的订单可以发货");
        }
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = LocalDateTime.now();
        registerEvent(new OrderShippedEvent(this.id.getValue(), this.orderNo.getValue(), trackingNo));
    }

    /**
     * 收货完成
     */
    public void complete() {
        if (status != OrderStatus.SHIPPED) {
            throw new BusinessException("ORDER_STATUS_ERROR", "只有已发货的订单可以完成");
        }
        this.status = OrderStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
        registerEvent(new OrderCompletedEvent(this.id.getValue(), this.orderNo.getValue()));
    }

    @Override
    public String getAggregateType() {
        return "Order";
    }

    /**
     * 订单ID
     */
    @Data
    @ValueObject
    public static class OrderId {
        private final String value;

        public static OrderId of(String value) {
            return new OrderId(value);
        }

        public static OrderId generate() {
            return new OrderId(IdGenerator.generateUUID());
        }
    }

    /**
     * 订单号
     */
    @Data
    @ValueObject
    public static class OrderNo {
        private final String value;

        public static OrderNo of(String value) {
            return new OrderNo(value);
        }

        public static OrderNo generate() {
            return new OrderNo(IdGenerator.generateOrderNo());
        }
    }

    /**
     * 订单状态
     */
    @Getter
    @ValueObject
    public static class OrderStatus {
        public static final OrderStatus PENDING = new OrderStatus("PENDING");
        public static final OrderStatus CONFIRMED = new OrderStatus("CONFIRMED");
        public static final OrderStatus SHIPPED = new OrderStatus("SHIPPED");
        public static final OrderStatus COMPLETED = new OrderStatus("COMPLETED");
        public static final OrderStatus CANCELLED = new OrderStatus("CANCELLED");

        private final String value;

        private OrderStatus(String value) {
            this.value = value;
        }

        public static OrderStatus of(String value) {
            return switch (value.toUpperCase()) {
                case "PENDING" -> PENDING;
                case "CONFIRMED" -> CONFIRMED;
                case "SHIPPED" -> SHIPPED;
                case "COMPLETED" -> COMPLETED;
                case "CANCELLED" -> CANCELLED;
                default -> throw new BusinessException("ORDER_STATUS_INVALID", "无效的订单状态");
            };
        }
    }
}
