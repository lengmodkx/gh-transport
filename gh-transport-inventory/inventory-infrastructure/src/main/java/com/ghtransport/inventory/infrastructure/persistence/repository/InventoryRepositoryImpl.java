package com.ghtransport.inventory.infrastructure.persistence.repository;

import com.ghtransport.common.core.result.PageResult;
import com.ghtransport.inventory.domain.aggregate.Inventory;
import com.ghtransport.inventory.domain.repository.InventoryRepository;
import com.ghtransport.inventory.infrastructure.persistence.mapper.InventoryMapper;
import com.ghtransport.inventory.infrastructure.persistence.po.InventoryPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 库存仓储实现
 */
@Slf4j
@Repository
public class InventoryRepositoryImpl implements InventoryRepository {

    private final InventoryMapper inventoryMapper;

    public InventoryRepositoryImpl(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public Optional<Inventory> findById(Inventory.InventoryId id) {
        return inventoryMapper.findById(id.getValue())
                .map(inventoryMapper::toAggregate);
    }

    @Override
    public Optional<Inventory> findBySkuCode(String skuCode) {
        return inventoryMapper.findBySkuCode(skuCode)
                .map(inventoryMapper::toAggregate);
    }

    @Override
    public List<Inventory> findByWarehouseId(String warehouseId) {
        return inventoryMapper.findByWarehouseId(warehouseId)
                .stream()
                .map(inventoryMapper::toAggregate)
                .toList();
    }

    @Override
    public PageResult<Inventory> findByWarehouseId(String warehouseId, int pageNum, int pageSize) {
        var page = inventoryMapper.findByWarehouseIdPaged(warehouseId, pageNum, pageSize);
        return new PageResult<>(
                page.getList().stream().map(inventoryMapper::toAggregate).toList(),
                page.getTotal(),
                page.getPageNum(),
                page.getPageSize()
        );
    }

    @Override
    public PageResult<Inventory> findByStatus(Inventory.InventoryStatus status, int pageNum, int pageSize) {
        var page = inventoryMapper.findByStatus(status.getValue(), pageNum, pageSize);
        return new PageResult<>(
                page.getList().stream().map(inventoryMapper::toAggregate).toList(),
                page.getTotal(),
                page.getPageNum(),
                page.getPageSize()
        );
    }

    @Override
    public PageResult<Inventory> search(String keyword, int pageNum, int pageSize) {
        var page = inventoryMapper.search(keyword, pageNum, pageSize);
        return new PageResult<>(
                page.getList().stream().map(inventoryMapper::toAggregate).toList(),
                page.getTotal(),
                page.getPageNum(),
                page.getPageSize()
        );
    }

    @Override
    public void save(Inventory inventory) {
        InventoryPO po = inventoryMapper.toPO(inventory);
        boolean exists = inventoryMapper.findById(po.getId()).isPresent();
        if (exists) {
            inventoryMapper.update(po);
            log.debug("库存更新成功: {}", inventory.getId().getValue());
        } else {
            inventoryMapper.insert(po);
            log.debug("库存保存成功: {}", inventory.getId().getValue());
        }
    }

    @Override
    public void delete(Inventory.InventoryId id) {
        inventoryMapper.delete(id.getValue());
        log.debug("库存删除成功: {}", id.getValue());
    }

    @Override
    public boolean existsBySkuCode(String skuCode) {
        return inventoryMapper.existsBySkuCode(skuCode);
    }
}
