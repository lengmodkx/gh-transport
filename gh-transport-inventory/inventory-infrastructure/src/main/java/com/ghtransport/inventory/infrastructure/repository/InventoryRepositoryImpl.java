package com.ghtransport.inventory.infrastructure.repository;

import com.ghtransport.common.core.result.PageResult;
import com.ghtransport.inventory.domain.aggregate.Inventory;
import com.ghtransport.inventory.domain.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 库存仓储实现
 */
@Slf4j
@Repository
public class InventoryRepositoryImpl implements InventoryRepository {

    private final List<Inventory> inventoryStore = new ArrayList<>();

    @Override
    public Optional<Inventory> findById(Inventory.InventoryId id) {
        return inventoryStore.stream()
                .filter(i -> i.getId().getValue().equals(id.getValue()))
                .findFirst();
    }

    @Override
    public Optional<Inventory> findBySkuCode(String skuCode) {
        return inventoryStore.stream()
                .filter(i -> i.getSkuCode().equals(skuCode))
                .findFirst();
    }

    @Override
    public List<Inventory> findByWarehouseId(String warehouseId) {
        return inventoryStore.stream()
                .filter(i -> warehouseId.equals(i.getWarehouseId()))
                .toList();
    }

    @Override
    public PageResult<Inventory> findByWarehouseId(String warehouseId, int pageNum, int pageSize) {
        List<Inventory> filtered = findByWarehouseId(warehouseId);
        return createPageResult(filtered, pageNum, pageSize);
    }

    @Override
    public PageResult<Inventory> findByStatus(Inventory.InventoryStatus status, int pageNum, int pageSize) {
        List<Inventory> filtered = inventoryStore.stream()
                .filter(i -> i.getStatus() == status)
                .toList();
        return createPageResult(filtered, pageNum, pageSize);
    }

    @Override
    public PageResult<Inventory> search(String keyword, int pageNum, int pageSize) {
        List<Inventory> filtered = inventoryStore.stream()
                .filter(i -> i.getSkuCode().contains(keyword) || i.getProductName().contains(keyword))
                .toList();
        return createPageResult(filtered, pageNum, pageSize);
    }

    @Override
    public void save(Inventory inventory) {
        Optional<Inventory> existing = findById(inventory.getId());
        if (existing.isPresent()) {
            int index = inventoryStore.indexOf(existing.get());
            inventoryStore.set(index, inventory);
        } else {
            inventoryStore.add(inventory);
        }
        log.info("保存库存: {}", inventory.getSkuCode());
    }

    @Override
    public void delete(Inventory.InventoryId id) {
        inventoryStore.removeIf(i -> i.getId().getValue().equals(id.getValue()));
    }

    @Override
    public boolean existsBySkuCode(String skuCode) {
        return inventoryStore.stream().anyMatch(i -> i.getSkuCode().equals(skuCode));
    }

    private PageResult<Inventory> createPageResult(List<Inventory> list, int pageNum, int pageSize) {
        int total = list.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Inventory> pageList = start >= total ? List.of() : list.subList(start, end);
        return new PageResult<>(pageNum, pageSize, (long) total, pageList);
    }
}
