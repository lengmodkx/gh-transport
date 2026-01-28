package com.ghtransport.order.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order")
public class OrderPO {

    private String id;
    private String orderNo;
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
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String itemsJson;
}
