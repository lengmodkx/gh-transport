package com.ghtransport.transport.interfaces.controller;

import com.ghtransport.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 运输控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transport")
@Tag(name = "运输管理", description = "运输相关接口")
public class TransportController {

    @GetMapping("/{waybillId}")
    @Operation(summary = "获取运单详情")
    public Result<Map<String, Object>> getTransport(@PathVariable String waybillId) {
        return Result.success(Map.of("waybillId", waybillId));
    }

    @PostMapping("/{waybillId}/location")
    @Operation(summary = "更新位置")
    public Result<Map<String, Object>> updateLocation(@PathVariable String waybillId, @RequestBody LocationRequest request) {
        return Result.success(Map.of("waybillId", waybillId, "location", request));
    }

    @PostMapping("/{waybillId}/pickup")
    @Operation(summary = "提货")
    public Result<Map<String, Object>> pickup(@PathVariable String waybillId) {
        return Result.success(Map.of("waybillId", waybillId, "status", "PICKING_UP"));
    }

    @PostMapping("/{waybillId}/deliver")
    @Operation(summary = "开始配送")
    public Result<Map<String, Object>> startDelivery(@PathVariable String waybillId) {
        return Result.success(Map.of("waybillId", waybillId, "status", "DELIVERING"));
    }

    @PostMapping("/{waybillId}/complete")
    @Operation(summary = "完成配送")
    public Result<Map<String, Object>> completeDelivery(@PathVariable String waybillId) {
        return Result.success(Map.of("waybillId", waybillId, "status", "DELIVERED"));
    }

    public record LocationRequest(Double lng, Double lat, Double speed, Double direction) {}
}
