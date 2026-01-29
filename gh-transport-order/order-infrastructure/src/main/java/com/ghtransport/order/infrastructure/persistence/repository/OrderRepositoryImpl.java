package com.ghtransport.order.infrastructure.persistence.repository;

import com.ghtransport.common.core.result.PageResult;
import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.repository.OrderRepository;
import com.ghtransport.order.infrastructure.persistence.mapper.OrderMapper;
import com.ghtransport.order.infrastructure.persistence.po.OrderItemPO;
import com.ghtransport.order.infrastructure.persistence.po.OrderPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 订单仓储实现
 */
@Slf4j
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;

    public OrderRepositoryImpl(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Order.OrderId id) {
        Optional<OrderPO> poOpt = orderMapper.findById(id.getValue());
        if (poOpt.isEmpty()) {
            return Optional.empty();
        }
        OrderPO po = poOpt.get();
        return Optional.of(loadOrderWithItems(po));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByOrderNo(Order.OrderNo orderNo) {
        Optional<OrderPO> poOpt = orderMapper.findByOrderNo(orderNo.getValue());
        if (poOpt.isEmpty()) {
            return Optional.empty();
        }
        OrderPO po = poOpt.get();
        return Optional.of(loadOrderWithItems(po));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Order> findByCustomerId(String customerId, int pageNum, int pageSize) {
        List<OrderPO> pos = orderMapper.findByCustomerId(customerId, pageNum, pageSize);
        return toPageResult(pos, pageNum, pageSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Order> findByStatus(Order.OrderStatus status, int pageNum, int pageSize) {
        List<OrderPO> pos = orderMapper.findByStatus(status.getValue(), pageNum, pageSize);
        return toPageResult(pos, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Order order) {
        OrderPO po = orderMapper.toPO(order);
        boolean exists = orderMapper.findById(po.getId()).isPresent();
        if (exists) {
            orderMapper.update(po);
            orderMapper.deleteItems(po.getId());
            orderMapper.insertItems(orderMapper.formatOrderItems(order.getItems(), po.getId()));
            log.debug("订单更新成功: {}", order.getId().getValue());
        } else {
            orderMapper.insert(po);
            orderMapper.insertItems(orderMapper.formatOrderItems(order.getItems(), po.getId()));
            log.debug("订单保存成功: {}", order.getId().getValue());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Order.OrderId id) {
        orderMapper.deleteItems(id.getValue());
        orderMapper.delete(id.getValue());
        log.debug("订单删除成功: {}", id.getValue());
    }

    private Order loadOrderWithItems(OrderPO po) {
        Order order = orderMapper.toAggregate(po);
        List<OrderItemPO> items = orderMapper.findItemsByOrderId(po.getId());
        order.setItems(orderMapper.parseOrderItems(items));
        return order;
    }

    private PageResult<Order> toPageResult(List<OrderPO> pos, int pageNum, int pageSize) {
        List<Order> orders = pos.stream()
                .map(this::loadOrderWithItems)
                .toList();
        int total = pos.isEmpty() ? 0 :
                (pos.get(0).getCustomerId() != null ?
                        orderMapper.countByCustomerId(pos.get(0).getCustomerId()) :
                        orderMapper.countByStatus(pos.get(0).getStatus()));
        return new PageResult<>(orders, total, pageNum, pageSize);
    }
}
