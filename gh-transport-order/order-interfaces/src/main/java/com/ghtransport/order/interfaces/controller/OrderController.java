package com.ghtransport.order.interfaces.controller;

import com.ghtransport.order.application.command.CancelOrderCmd;
import com.ghtransport.order.application.command.CreateOrderCmd;
import com.ghtransport.order.application.service.OrderApplicationService;
import com.ghtransport.order.interfaces.assembler.OrderAssembler;
import com.ghtransport.order.interfaces.dto.CancelOrderRequest;
import com.ghtransport.order.interfaces.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderAppService;
    private final OrderAssembler orderAssembler;

    @PostMapping
    public Result<String> create(@RequestBody CreateOrderRequest request) {
        CreateOrderCmd cmd = orderAssembler.toCreateCmd(request);
        String orderId = orderAppService.createOrder(cmd);
        return Result.success(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable String orderId,
                               @RequestBody CancelOrderRequest request) {
        CancelOrderCmd cmd = new CancelOrderCmd();
        cmd.setOrderId(orderId);
        cmd.setReason(request.getReason());
        orderAppService.cancelOrder(cmd);
        return Result.success();
    }
}
