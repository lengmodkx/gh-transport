package com.ghtransport.inventory.domain.aggregate;

import com.ghtransport.common.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 库存聚合根测试
 */
class InventoryTest {

    @Test
    @DisplayName("创建库存 - 成功")
    void createInventory_Success() {
        // Given & When
        Inventory inventory = Inventory.create(
            "SKU-001",
            "测试商品",
            "wh-001",
            1000,
            BigDecimal.valueOf(99.99)
        );

        // Then
        assertNotNull(inventory.getId());
        assertEquals("SKU-001", inventory.getSkuCode());
        assertEquals("测试商品", inventory.getProductName());
        assertEquals("wh-001", inventory.getWarehouseId());
        assertEquals(1000, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(1000, inventory.getAvailableQuantity());
        assertEquals(Inventory.InventoryStatus.AVAILABLE, inventory.getStatus());
    }

    @Test
    @DisplayName("预留库存 - 成功")
    void reserve_Success() {
        // Given
        Inventory inventory = Inventory.create("SKU-001", "商品", "wh-001", 100, BigDecimal.TEN);

        // When
        boolean result = inventory.reserve(30);

        // Then
        assertTrue(result);
        assertEquals(30, inventory.getReservedQuantity());
        assertEquals(70, inventory.getAvailableQuantity());
    }

    @Test
    @DisplayName("预留库存 - 库存不足")
    void reserve_InsufficientStock() {
        // Given
        Inventory inventory = Inventory.create("SKU-001", "商品", "wh-001", 20, BigDecimal.TEN);

        // When
        boolean result = inventory.reserve(30);

        // Then
        assertFalse(result);
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("预留库存 - 超过可用数量")
    void reserve_ExceedAvailable() {
        // Given
        Inventory inventory = Inventory.create("SKU-001", "商品", "wh-001", 100, BigDecimal.TEN);
        inventory.reserve(80);  // 已预留80

        // When - 尝试再预留30（只有20可用）
        boolean result = inventory.reserve(30);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("扣减库存 - 成功")
    void deduct_Success() {
        // Given
        Inventory inventory = Inventory.create("SKU-001", "商品", "wh-001", 100, BigDecimal.TEN);
        inventory.reserve(20);

        // When
        inventory.deduct(20);

        // Then
        assertEquals(80, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());  // 释放预留
        assertEquals(80, inventory.getAvailableQuantity());
    }

    @Test
    @DisplayName("扣减库存 - 数量为0")
    void deduct_ZeroQuantity() {
        // Given
        Inventory inventory = Inventory.create("SKU-001", "商品", "wh-001", 100, BigDecimal.TEN);

        // When
        inventory.deduct(100);

        // Then
        assertEquals(0, inventory.getQuantity());
        assertEquals(Inventory.InventoryStatus.SOLD_OUT, inventory.getStatus());
    }

    @Test
    @DisplayName("释放库存 - 成功")
    void release_Success() {
        // Given
        Inventory inventory = Inventory.create("SKU-001", "商品", "wh-001", 100, BigDecimal.TEN);
        inventory.reserve(30);

        // When
        inventory.release(10);

        // Then
        assertEquals(20, inventory.getReservedQuantity());
        assertEquals(80, inventory.getAvailableQuantity());
    }

    @Test
    @DisplayName("释放库存 - 数量不能为负")
    void release_NeverNegative() {
        // Given
        Inventory inventory = Inventory.create("SKU-001", "商品", "wh-001", 100, BigDecimal.TEN);
        inventory.reserve(10);

        // When - 释放超过预留数量
        inventory.release(20);

        // Then - 应该为0，不能为负
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("获取可用数量")
    void getAvailableQuantity() {
        // Given
        Inventory inventory = Inventory.create("SKU-001", "商品", "wh-001", 100, BigDecimal.TEN);

        // Then - 无预留
        assertEquals(100, inventory.getAvailableQuantity());

        // When - 预留50
        inventory.reserve(50);

        // Then - 可用50
        assertEquals(50, inventory.getAvailableQuantity());
    }
}
