package com.ghtransport.inventory.application.service;

import com.ghtransport.inventory.domain.aggregate.Inventory;
import com.ghtransport.inventory.domain.repository.InventoryRepository;
import com.ghtransport.common.core.exception.BusinessException;
import com.ghtransport.common.core.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存应用服务
 */
@Slf4j
@Service
public class InventoryApplicationService {

    private final InventoryRepository inventoryRepository;

    public InventoryApplicationService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * 创建库存
     */
    @Transactional(rollbackFor = Exception.class)
    public Inventory createInventory(String skuCode, String productName, String warehouseId,
                                   Integer quantity, BigDecimal unitPrice) {
        if (inventoryRepository.existsBySkuCode(skuCode)) {
            throw new BusinessException("SKU_EXISTS", "SKU编码已存在");
        }
        Inventory inventory = Inventory.create(skuCode, productName, warehouseId, quantity, unitPrice);
        inventoryRepository.save(inventory);
        log.info("库存创建成功: {}", skuCode);
        return inventory;
    }

    /**
     * 获取库存详情
     */
    @Transactional(readOnly = true)
    public Inventory getInventory(String inventoryId) {
        return inventoryRepository.findById(Inventory.InventoryId.of(inventoryId))
                .orElseThrow(() -> new BusinessException("INVENTORY_NOT_FOUND", "库存不存在"));
    }

    /**
     * 预留库存
     */
    @Transactional(rollbackFor = Exception.class)
    public Inventory reserveInventory(String inventoryId, Integer quantity) {
        Inventory inventory = getInventory(inventoryId);
        if (!inventory.reserve(quantity)) {
            throw new BusinessException("INSUFFICIENT_STOCK", "库存不足");
        }
        inventoryRepository.save(inventory);
        log.info("库存预留成功: {}, 数量: {}", inventoryId, quantity);
        return inventory;
    }

    /**
     * 扣减库存
     */
    @Transactional(rollbackFor = Exception.class)
    public Inventory deductInventory(String inventoryId, Integer quantity) {
        Inventory inventory = getInventory(inventoryId);
        if (inventory.getAvailableQuantity() < quantity) {
            throw new BusinessException("INSUFFICIENT_STOCK", "可用库存不足");
        }
        inventory.deduct(quantity);
        inventoryRepository.save(inventory);
        log.info("库存扣减成功: {}, 数量: {}", inventoryId, quantity);
        return inventory;
    }

    /**
     * 释放库存
     */
    @Transactional(rollbackFor = Exception.class)
    public Inventory releaseInventory(String inventoryId, Integer quantity) {
        Inventory inventory = getInventory(inventoryId);
        inventory.release(quantity);
        inventoryRepository.save(inventory);
        log.info("库存释放成功: {}, 数量: {}", inventoryId, quantity);
        return inventory;
    }

    /**
     * 获取仓库库存列表
     */
    @Transactional(readOnly = true)
    public PageResult<Inventory> getWarehouseInventory(String warehouseId, int pageNum, int pageSize) {
        return inventoryRepository.findByWarehouseId(warehouseId, pageNum, pageSize);
    }

    /**
     * 搜索库存
     */
    @Transactional(readOnly = true)
    public PageResult<Inventory> searchInventory(String keyword, int pageNum, int pageSize) {
        return inventoryRepository.search(keyword, pageNum, pageSize);
    }
}
