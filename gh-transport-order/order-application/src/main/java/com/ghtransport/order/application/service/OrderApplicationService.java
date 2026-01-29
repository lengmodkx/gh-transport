package com.ghtransport.order.application.service;

import com.ghtransport.order.application.command.CreateOrderCmd;
import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.repository.OrderRepository;
import com.ghtransport.order.domain.valueobject.Address;
import com.ghtransport.common.core.exception.BusinessException;
import com.ghtransport.common.core.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单应用服务
 */
@Slf4j
@Service
public class OrderApplicationService {

    private final OrderRepository orderRepository;

    public OrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * 创建订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(CreateOrderCmd cmd) {
        // 构建地址
        Address address = Address.of(
                cmd.getProvince(), cmd.getCity(),
                cmd.getDistrict() != null ? cmd.getDistrict() : "",
                cmd.getDetail(), cmd.getReceiverName(), cmd.getReceiverPhone(),
                cmd.getPostalCode() != null ? cmd.getPostalCode() : ""
        );

        // 构建订单明细
        List<Order.OrderItem> items = cmd.getItems().stream()
                .map(itemCmd -> new Order.OrderItem(
                        itemCmd.getProductId(),
                        itemCmd.getProductName(),
                        itemCmd.getSpecification() != null ? itemCmd.getSpecification() : "",
                        Order.OrderItem.Money.of(itemCmd.getPrice()),
                        Order.OrderItem.Quantity.of(Integer.parseInt(itemCmd.getQuantity()))
                )
                .toList();

        // 创建订单
        Order order = Order.create(cmd.getCustomerId(), address, items, cmd.getRemark());

        // 保存订单
        orderRepository.save(order);

        log.info("订单创建成功: {}", order.getOrderNo().getValue());

        return order;
    }

    /**
     * 获取订单详情
     */
    @Transactional(readOnly = true)
    public Order getOrder(String orderId) {
        return orderRepository.findById(Order.OrderId.of(orderId))
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "订单不存在: " + orderId));
    }

    /**
     * 根据订单号获取订单
     */
    @Transactional(readOnly = true)
    public Order getOrderByNo(String orderNo) {
        return orderRepository.findByOrderNo(Order.OrderNo.of(orderNo))
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "订单不存在: " + orderNo));
    }

    /**
     * 确认订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order confirmOrder(String orderId) {
        Order order = getOrder(orderId);
        order.confirm();
        orderRepository.save(order);
        log.info("订单确认成功: {}", order.getOrderNo().getValue());
        return order;
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order cancelOrder(String orderId, String reason) {
        Order order = getOrder(orderId);
        order.cancel(reason);
        orderRepository.save(order);
        log.info("订单取消成功: {}", order.getOrderNo().getValue());
        return order;
    }

    /**
     * 发货
     */
    @Transactional(rollbackFor = Exception.class)
    public Order shipOrder(String orderId, String trackingNo) {
        Order order = getOrder(orderId);
        order.ship(trackingNo);
        orderRepository.save(order);
        log.info("订单发货成功: {}, 运单号: {}", order.getOrderNo().getValue(), trackingNo);
        return order;
    }

    /**
     * 完成订单
     */
    @Transactional(rollbackFor = Exception.class)
    public Order completeOrder(String orderId) {
        Order order = getOrder(orderId);
        order.complete();
        orderRepository.save(order);
        log.info("订单完成: {}", order.getOrderNo().getValue());
        return order;
    }

    /**
     * 获取客户订单列表
     */
    @Transactional(readOnly = true)
    public PageResult<Order> getCustomerOrders(String customerId, int pageNum, int pageSize) {
        return orderRepository.findByCustomerId(customerId, pageNum, pageSize);
    }

    /**
     * 获取订单列表（按状态）
     */
    @Transactional(readOnly = true)
    public PageResult<Order> getOrdersByStatus(String status, int pageNum, int pageSize) {
        return orderRepository.findByStatus(Order.OrderStatus.of(status), pageNum, pageSize);
    }
}
