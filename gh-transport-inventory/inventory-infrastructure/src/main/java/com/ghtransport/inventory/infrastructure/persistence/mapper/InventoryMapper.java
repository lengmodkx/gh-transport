package com.ghtransport.inventory.infrastructure.persistence.mapper;

import com.ghtransport.common.core.result.PageResult;
import com.ghtransport.inventory.domain.aggregate.Inventory;
import com.ghtransport.inventory.domain.aggregate.Inventory.InventoryId;
import com.ghtransport.inventory.domain.aggregate.Inventory.InventoryStatus;
import com.ghtransport.inventory.infrastructure.persistence.po.InventoryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 库存MyBatis Mapper
 */
@Mapper
public interface InventoryMapper {

    void insert(@Param("po") InventoryPO po);

    void update(@Param("po") InventoryPO po);

    void delete(@Param("id") String id);

    Optional<InventoryPO> findById(@Param("id") String id);

    Optional<InventoryPO> findBySkuCode(@Param("skuCode") String skuCode);

    List<InventoryPO> findByWarehouseId(@Param("warehouseId") String warehouseId);

    PageResult<InventoryPO> findByWarehouseIdPaged(@Param("warehouseId") String warehouseId, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    PageResult<InventoryPO> findByStatus(@Param("status") String status, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    PageResult<InventoryPO> search(@Param("keyword") String keyword, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    boolean existsBySkuCode(@Param("skuCode") String skuCode);

    /**
     * 将PO转换为Aggregate
     */
    default Inventory toAggregate(InventoryPO po) {
        if (po == null) return null;
        Inventory inventory = new Inventory();
        inventory.setId(InventoryId.of(po.getId()));
        inventory.setSkuCode(po.getSkuCode());
        inventory.setProductName(po.getProductName());
        inventory.setWarehouseId(po.getWarehouseId());
        inventory.setQuantity(po.getQuantity());
        inventory.setReservedQuantity(po.getReservedQuantity());
        inventory.setUnitPrice(po.getUnitPrice());
        inventory.setStatus(InventoryStatus.of(po.getStatus()));
        inventory.setCreatedAt(po.getCreatedAt());
        inventory.setUpdatedAt(po.getUpdatedAt());
        return inventory;
    }

    /**
     * 将Aggregate转换为PO
     */
    default InventoryPO toPO(Inventory inventory) {
        if (inventory == null) return null;
        InventoryPO po = new InventoryPO();
        po.setId(inventory.getId().getValue());
        po.setSkuCode(inventory.getSkuCode());
        po.setProductName(inventory.getProductName());
        po.setWarehouseId(inventory.getWarehouseId());
        po.setQuantity(inventory.getQuantity());
        po.setReservedQuantity(inventory.getReservedQuantity());
        po.setUnitPrice(inventory.getUnitPrice());
        po.setStatus(inventory.getStatus().getValue());
        po.setCreatedAt(inventory.getCreatedAt());
        po.setUpdatedAt(inventory.getUpdatedAt());
        return po;
    }
}
