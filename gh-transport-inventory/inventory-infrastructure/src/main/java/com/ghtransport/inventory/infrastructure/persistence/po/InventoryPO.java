package com.ghtransport.inventory.infrastructure.persistence.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存持久化对象
 */
@Data
public class InventoryPO {
    private String id;
    private String skuCode;
    private String productName;
    private String warehouseId;
    private Integer quantity;
    private Integer reservedQuantity;
    private BigDecimal unitPrice;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
