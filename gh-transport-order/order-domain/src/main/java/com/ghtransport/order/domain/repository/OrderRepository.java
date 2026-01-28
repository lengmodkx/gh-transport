package com.ghtransport.order.domain.repository;

import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.vo.OrderStatus;

import java.util.List;

public interface OrderRepository {

    Order findById(String id);

    List<Order> findByCustomerId(String customerId);

    List<Order> findByStatus(OrderStatus status);

    void save(Order order);
}
