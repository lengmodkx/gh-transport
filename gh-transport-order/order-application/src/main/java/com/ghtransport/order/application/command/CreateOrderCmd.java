package com.ghtransport.order.application.command;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateOrderCmd {
    private String customerId;
    private String pickupProvince;
    private String pickupCity;
    private String pickupDistrict;
    private String pickupDetail;
    private String pickupContact;
    private String pickupPhone;
    private String deliveryProvince;
    private String deliveryCity;
    private String deliveryDistrict;
    private String deliveryDetail;
    private String deliveryContact;
    private String deliveryPhone;
    private List<OrderItemCmd> items;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
}
