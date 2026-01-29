package com.ghtransport.customer.interfaces.controller;

import com.ghtransport.common.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 客户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "客户管理", description = "客户相关接口")
public class CustomerController {

    @PostMapping
    @Operation(summary = "创建客户")
    public Result<Map<String, Object>> createCustomer(@RequestBody CreateCustomerRequest request) {
        log.info("创建客户: {}", request);
        return Result.success(Map.of("message", "客户创建成功"));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "获取客户详情")
    public Result<Map<String, Object>> getCustomer(@PathVariable String customerId) {
        return Result.success(Map.of("customerId", customerId));
    }

    @GetMapping
    @Operation(summary = "获取客户列表")
    public Result<Map<String, Object>> listCustomers(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(Map.of("pageNum", pageNum, "pageSize", pageSize));
    }

    @PutMapping("/{customerId}")
    @Operation(summary = "更新客户")
    public Result<Map<String, Object>> updateCustomer(@PathVariable String customerId,
                                                       @RequestBody UpdateCustomerRequest request) {
        return Result.success(Map.of("customerId", customerId, "message", "客户更新成功"));
    }

    @PostMapping("/{customerId}/disable")
    @Operation(summary = "禁用客户")
    public Result<Map<String, Object>> disableCustomer(@PathVariable String customerId) {
        return Result.success(Map.of("customerId", customerId, "status", "INACTIVE"));
    }

    @PostMapping("/{customerId}/enable")
    @Operation(summary = "启用客户")
    public Result<Map<String, Object>> enableCustomer(@PathVariable String customerId) {
        return Result.success(Map.of("customerId", customerId, "status", "ACTIVE"));
    }

    public record CreateCustomerRequest(String name, String contactPerson, String phone, String email, String address, String type) {}
    public record UpdateCustomerRequest(String name, String contactPerson, String phone, String email, String address) {}
}
