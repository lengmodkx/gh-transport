package com.ghtransport.order.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.repository.OrderRepository;
import com.ghtransport.order.domain.vo.Address;
import com.ghtransport.order.domain.vo.OrderStatus;
import com.ghtransport.order.infrastructure.persistence.mapper.OrderMapper;
import com.ghtransport.order.infrastructure.persistence.po.OrderPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;

    @Override
    public Order findById(String id) {
        OrderPO po = orderMapper.selectById(id);
        return po != null ? OrderConverter.toDomain(po) : null;
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderPO::getCustomerId, customerId);
        return orderMapper.selectList(wrapper).stream()
            .map(OrderConverter::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderPO::getStatus, status.name());
        return orderMapper.selectList(wrapper).stream()
            .map(OrderConverter::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void save(Order order) {
        OrderPO po = OrderConverter.toPO(order);
        if (order.isNew()) {
            orderMapper.insert(po);
        } else {
            orderMapper.updateById(po);
        }
    }

    static class OrderConverter {
        static Order toDomain(OrderPO po) {
            Order order = Order.create(
                po.getCustomerId(),
                new ArrayList<>(),
                new Address(
                    po.getPickupProvince(), po.getPickupCity(),
                    po.getPickupDistrict(), po.getPickupDetail(),
                    po.getPickupContact(), po.getPickupPhone()
                ),
                new Address(
                    po.getDeliveryProvince(), po.getDeliveryCity(),
                    po.getDeliveryDistrict(), po.getDeliveryDetail(),
                    po.getDeliveryContact(), po.getDeliveryPhone()
                ),
                po.getPickupTime(),
                po.getDeliveryTime()
            );
            return order;
        }

        static OrderPO toPO(Order order) {
            OrderPO po = new OrderPO();
            po.setId(order.getId());
            po.setOrderNo(order.getOrderNo().getValue());
            po.setCustomerId(order.getCustomerId());
            po.setPickupProvince(order.getPickupAddress().getProvince());
            po.setPickupCity(order.getPickupAddress().getCity());
            po.setPickupDistrict(order.getPickupAddress().getDistrict());
            po.setPickupDetail(order.getPickupAddress().getDetail());
            po.setPickupContact(order.getPickupAddress().getContactName());
            po.setPickupPhone(order.getPickupAddress().getContactPhone());
            po.setDeliveryProvince(order.getDeliveryAddress().getProvince());
            po.setDeliveryCity(order.getDeliveryAddress().getCity());
            po.setDeliveryDistrict(order.getDeliveryAddress().getDistrict());
            po.setDeliveryDetail(order.getDeliveryAddress().getDetail());
            po.setDeliveryContact(order.getDeliveryAddress().getContactName());
            po.setDeliveryPhone(order.getDeliveryAddress().getContactPhone());
            po.setTotalAmount(order.getTotalAmount().getAmount());
            po.setStatus(order.getStatus().name());
            po.setPickupTime(order.getPickupTime());
            po.setDeliveryTime(order.getDeliveryTime());
            po.setCreatedAt(order.getCreatedAt());
            po.setUpdatedAt(order.getUpdatedAt());
            return po;
        }
    }
}
