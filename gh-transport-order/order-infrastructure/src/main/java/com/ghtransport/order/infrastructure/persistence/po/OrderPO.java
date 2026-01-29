package com.ghtransport.order.infrastructure.persistence.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单持久化对象
 */
@Data
public class OrderPO {
    private String id;
    private String orderNo;
    private String customerId;
    private String shippingAddress;
    private String status;
    private BigDecimal totalAmount;
    private String remark;
    private List<OrderItemPO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
