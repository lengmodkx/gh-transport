package com.ghtransport.order.interfaces.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private String itemName;
    private int quantity;
    private BigDecimal weight;
    private BigDecimal volume;
    private BigDecimal unitPrice;
}
