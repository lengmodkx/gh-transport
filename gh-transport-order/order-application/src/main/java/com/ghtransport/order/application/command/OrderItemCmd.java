package com.ghtransport.order.application.command;

import lombok.Data;

@Data
public class OrderItemCmd {
    private String itemName;
    private int quantity;
    private java.math.BigDecimal weight;
    private java.math.BigDecimal volume;
    private java.math.BigDecimal unitPrice;
}
