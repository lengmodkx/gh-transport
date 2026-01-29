package com.ghtransport.order.domain.repository;

import com.ghtransport.common.core.result.PageResult;
import com.ghtransport.order.domain.aggregate.Order;

import java.util.Optional;

/**
 * 订单仓储接口
 */
public interface OrderRepository {

    Optional<Order> findById(Order.OrderId id);

    Optional<Order> findByOrderNo(Order.OrderNo orderNo);

    PageResult<Order> findByCustomerId(String customerId, int pageNum, int pageSize);

    PageResult<Order> findByStatus(Order.OrderStatus status, int pageNum, int pageSize);

    void save(Order order);

    void delete(Order.OrderId id);
}
