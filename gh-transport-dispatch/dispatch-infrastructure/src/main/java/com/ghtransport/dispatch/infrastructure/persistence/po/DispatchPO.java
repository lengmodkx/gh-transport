package com.ghtransport.dispatch.infrastructure.persistence.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调度单持久化对象
 */
@Data
public class DispatchPO {
    private String id;
    private String dispatchNo;
    private String orderId;
    private String vehicleId;
    private String driverId;
    private String status;
    private LocalDateTime plannedTime;
    private LocalDateTime actualTime;
    private String originAddress;
    private String destinationAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
