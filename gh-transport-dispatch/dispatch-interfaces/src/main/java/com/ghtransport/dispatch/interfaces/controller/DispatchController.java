package com.ghtransport.dispatch.interfaces.controller;

import com.ghtransport.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 调度控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dispatch")
@Tag(name = "调度管理", description = "调度相关接口")
public class DispatchController {

    @PostMapping
    @Operation(summary = "创建调度单")
    public Result<Map<String, Object>> createDispatch(@RequestBody CreateDispatchRequest request) {
        log.info("创建调度单: {}", request);
        return Result.success(Map.of("message", "调度单创建成功"));
    }

    @GetMapping("/{dispatchId}")
    @Operation(summary = "获取调度单详情")
    public Result<Map<String, Object>> getDispatch(@PathVariable String dispatchId) {
        return Result.success(Map.of("dispatchId", dispatchId));
    }

    @PostMapping("/{dispatchId}/assign")
    @Operation(summary = "分配调度")
    public Result<Map<String, Object>> assignDispatch(@PathVariable String dispatchId) {
        return Result.success(Map.of("dispatchId", dispatchId, "status", "ASSIGNED"));
    }

    @PostMapping("/{dispatchId}/start")
    @Operation(summary = "开始运输")
    public Result<Map<String, Object>> startTransport(@PathVariable String dispatchId) {
        return Result.success(Map.of("dispatchId", dispatchId, "status", "IN_TRANSIT"));
    }

    @PostMapping("/{dispatchId}/complete")
    @Operation(summary = "完成调度")
    public Result<Map<String, Object>> completeDispatch(@PathVariable String dispatchId) {
        return Result.success(Map.of("dispatchId", dispatchId, "status", "COMPLETED"));
    }

    public record CreateDispatchRequest(String orderId, String vehicleId, String driverId, String plannedTime) {}
}
