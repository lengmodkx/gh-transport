package com.ghtransport.inventory.domain.repository;

import com.ghtransport.inventory.domain.aggregate.Inventory;
import com.ghtransport.common.core.result.PageResult;

import java.util.List;
import java.util.Optional;

/**
 * 库存仓储接口
 */
public interface InventoryRepository {

    Optional<Inventory> findById(Inventory.InventoryId id);

    Optional<Inventory> findBySkuCode(String skuCode);

    List<Inventory> findByWarehouseId(String warehouseId);

    PageResult<Inventory> findByWarehouseId(String warehouseId, int pageNum, int pageSize);

    PageResult<Inventory> findByStatus(Inventory.InventoryStatus status, int pageNum, int pageSize);

    PageResult<Inventory> search(String keyword, int pageNum, int pageSize);

    void save(Inventory inventory);

    void delete(Inventory.InventoryId id);

    boolean existsBySkuCode(String skuCode);
}
