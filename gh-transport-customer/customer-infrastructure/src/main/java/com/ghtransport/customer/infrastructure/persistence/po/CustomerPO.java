package com.ghtransport.customer.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户持久化对象
 */
@Data
public class CustomerPO {
    private String id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
