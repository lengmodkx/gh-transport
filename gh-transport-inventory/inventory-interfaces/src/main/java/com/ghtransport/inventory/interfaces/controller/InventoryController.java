package com.ghtransport.inventory.interfaces.controller;

import com.ghtransport.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 库存控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "库存管理", description = "库存相关接口")
public class InventoryController {

    @PostMapping
    @Operation(summary = "创建库存")
    public Result<Map<String, Object>> createInventory(@RequestBody CreateInventoryRequest request) {
        log.info("创建库存: {}", request);
        return Result.success(Map.of("message", "库存创建成功"));
    }

    @GetMapping("/{inventoryId}")
    @Operation(summary = "获取库存详情")
    public Result<Map<String, Object>> getInventory(@PathVariable String inventoryId) {
        return Result.success(Map.of("inventoryId", inventoryId));
    }

    @GetMapping
    @Operation(summary = "获取库存列表")
    public Result<Map<String, Object>> listInventory(@RequestParam(required = false) String warehouseId,
                                                      @RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(Map.of("warehouseId", warehouseId, "pageNum", pageNum, "pageSize", pageSize));
    }

    @PostMapping("/{inventoryId}/reserve")
    @Operation(summary = "预留库存")
    public Result<Map<String, Object>> reserve(@PathVariable String inventoryId, @RequestBody ReserveRequest request) {
        return Result.success(Map.of("inventoryId", inventoryId, "quantity", request.quantity(), "message", "预留成功"));
    }

    @PostMapping("/{inventoryId}/deduct")
    @Operation(summary = "扣减库存")
    public Result<Map<String, Object>> deduct(@PathVariable String inventoryId, @RequestBody ReserveRequest request) {
        return Result.success(Map.of("inventoryId", inventoryId, "quantity", request.quantity(), "message", "扣减成功"));
    }

    @PostMapping("/{inventoryId}/release")
    @Operation(summary = "释放库存")
    public Result<Map<String, Object>> release(@PathVariable String inventoryId, @RequestBody ReserveRequest request) {
        return Result.success(Map.of("inventoryId", inventoryId, "quantity", request.quantity(), "message", "释放成功"));
    }

    public record CreateInventoryRequest(String skuCode, String productName, String warehouseId, Integer quantity, String unitPrice) {}
    public record ReserveRequest(Integer quantity) {}
}
