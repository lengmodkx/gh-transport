package com.ghtransport.order.infrastructure.persistence.po;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细持久化对象
 */
@Data
public class OrderItemPO {
    private String id;
    private String orderId;
    private String productId;
    private String productName;
    private String specification;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
