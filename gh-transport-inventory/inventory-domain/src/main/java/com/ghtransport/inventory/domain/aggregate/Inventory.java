package com.ghtransport.inventory.domain.aggregate;

import com.ghtransport.common.core.ddd.AggregateRoot;
import com.ghtransport.common.core.ddd.ValueObject;
import com.ghtransport.common.core.util.IdGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存聚合根
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class Inventory extends AggregateRoot<InventoryId> {

    private String skuCode;
    private String productName;
    private String warehouseId;
    private Integer quantity;
    private Integer reservedQuantity;
    private BigDecimal unitPrice;
    private InventoryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Inventory() { super(); }

    public static Inventory create(String skuCode, String productName, String warehouseId,
                                    Integer quantity, BigDecimal unitPrice) {
        Inventory i = new Inventory(InventoryId.generate());
        i.skuCode = skuCode;
        i.productName = productName;
        i.warehouseId = warehouseId;
        i.quantity = quantity;
        i.reservedQuantity = 0;
        i.unitPrice = unitPrice;
        i.status = InventoryStatus.AVAILABLE;
        i.createdAt = LocalDateTime.now();
        i.updatedAt = LocalDateTime.now();
        return i;
    }

    public boolean reserve(int qty) {
        if (getAvailableQuantity() >= qty) {
            this.reservedQuantity += qty;
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    public void deduct(int qty) {
        this.quantity -= qty;
        this.reservedQuantity = Math.max(0, this.reservedQuantity - qty);
        this.updatedAt = LocalDateTime.now();
    }

    public void release(int qty) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - qty);
        this.updatedAt = LocalDateTime.now();
    }

    public int getAvailableQuantity() {
        return this.quantity - this.reservedQuantity;
    }

    @Override public String getAggregateType() { return "Inventory"; }

    @Data @ValueObject public static class InventoryId { private final String value; public static InventoryId of(String v){return new InventoryId(v);} public static InventoryId generate(){return new InventoryId(IdGenerator.generateUUID());} }
    @Getter @ValueObject public static class InventoryStatus {
        public static final InventoryStatus AVAILABLE = new InventoryStatus("AVAILABLE");
        public static final InventoryStatus RESERVED = new InventoryStatus("RESERVED");
        public static final InventoryStatus SOLD_OUT = new InventoryStatus("SOLD_OUT");
        private final String value;
        private InventoryStatus(String v){this.value=v;}
        public static InventoryStatus of(String v){
            if (v == null) {
                throw new IllegalArgumentException("库存状态不能为空");
            }
            return switch(v.toUpperCase()){
                case "AVAILABLE" -> AVAILABLE;
                case "RESERVED" -> RESERVED;
                case "SOLD_OUT" -> SOLD_OUT;
                default -> throw new IllegalArgumentException("无效的库存状态: " + v);
            };
        }
    }
}
