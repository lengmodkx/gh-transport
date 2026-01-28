package com.ghtransport.order.application.service;

import com.ghtransport.common.domain.DomainEventPublisher;
import com.ghtransport.common.exception.DomainException;
import com.ghtransport.order.application.command.CancelOrderCmd;
import com.ghtransport.order.application.command.CreateOrderCmd;
import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.entity.OrderItem;
import com.ghtransport.order.domain.repository.OrderRepository;
import com.ghtransport.order.domain.vo.Address;
import com.ghtransport.order.domain.vo.Money;
import com.ghtransport.order.domain.vo.Quantity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public String createOrder(CreateOrderCmd cmd) {
        // 构建值对象
        Address pickupAddress = new Address(
            cmd.getPickupProvince(), cmd.getPickupCity(),
            cmd.getPickupDistrict(), cmd.getPickupDetail(),
            cmd.getPickupContact(), cmd.getPickupPhone()
        );

        Address deliveryAddress = new Address(
            cmd.getDeliveryProvince(), cmd.getDeliveryCity(),
            cmd.getDeliveryDistrict(), cmd.getDeliveryDetail(),
            cmd.getDeliveryContact(), cmd.getDeliveryPhone()
        );

        // 构建货物明细
        var items = cmd.getItems().stream()
            .map(itemCmd -> OrderItem.create(
                itemCmd.getItemName(),
                new Quantity(itemCmd.getQuantity()),
                itemCmd.getWeight(),
                itemCmd.getVolume(),
                new Money(itemCmd.getUnitPrice())
            ))
            .collect(Collectors.toList());

        // 创建聚合根
        Order order = Order.create(
            cmd.getCustomerId(),
            items,
            pickupAddress,
            deliveryAddress,
            cmd.getPickupTime(),
            cmd.getDeliveryTime()
        );

        // 持久化
        orderRepository.save(order);

        // 发布领域事件
        order.getDomainEvents().forEach(eventPublisher::publish);
        order.clearDomainEvents();

        return order.getId();
    }

    @Transactional
    public void cancelOrder(CancelOrderCmd cmd) {
        Order order = orderRepository.findById(cmd.getOrderId());
        if (order == null) {
            throw new DomainException("订单不存在");
        }

        order.cancel(cmd.getReason());
        orderRepository.save(order);

        // 发布领域事件
        order.getDomainEvents().forEach(eventPublisher::publish);
        order.clearDomainEvents();
    }
}
