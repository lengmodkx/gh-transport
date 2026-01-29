package com.ghtransport.order.infrastructure.persistence.mapper;

import com.ghtransport.order.domain.aggregate.Order;
import com.ghtransport.order.domain.aggregate.Order.OrderId;
import com.ghtransport.order.domain.aggregate.Order.OrderNo;
import com.ghtransport.order.domain.aggregate.Order.OrderStatus;
import com.ghtransport.order.domain.entity.OrderItem;
import com.ghtransport.order.domain.entity.OrderItem.Money;
import com.ghtransport.order.domain.entity.OrderItem.Quantity;
import com.ghtransport.order.domain.valueobject.Address;
import com.ghtransport.order.infrastructure.persistence.po.OrderItemPO;
import com.ghtransport.order.infrastructure.persistence.po.OrderPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 订单MyBatis Mapper
 */
@Mapper
public interface OrderMapper {

    void insert(@Param("po") OrderPO po);

    void insertItems(@Param("items") List<OrderItemPO> items);

    void update(@Param("po") OrderPO po);

    void delete(@Param("id") String id);

    void deleteItems(@Param("orderId") String orderId);

    Optional<OrderPO> findById(@Param("id") String id);

    Optional<OrderPO> findByOrderNo(@Param("orderNo") String orderNo);

    List<OrderPO> findByCustomerId(@Param("customerId") String customerId, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    List<OrderPO> findByStatus(@Param("status") String status, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    /**
     * 将PO转换为Aggregate
     */
    default Order toAggregate(OrderPO po) {
        if (po == null) return null;
        Order order = new Order();
        order.setId(OrderId.of(po.getId()));
        order.setOrderNo(OrderNo.of(po.getOrderNo()));
        order.setCustomerId(po.getCustomerId());
        order.setShippingAddress(parseAddress(po.getShippingAddress()));
        order.setStatus(OrderStatus.of(po.getStatus()));
        order.setTotalAmount(Money.of(po.getTotalAmount()));
        order.setRemark(po.getRemark());
        order.setItems(parseOrderItems(po.getItems()));
        order.setCreatedAt(po.getCreatedAt());
        order.setUpdatedAt(po.getUpdatedAt());
        return order;
    }

    /**
     * 将Aggregate转换为PO
     */
    default OrderPO toPO(Order order) {
        if (order == null) return null;
        OrderPO po = new OrderPO();
        po.setId(order.getId().getValue());
        po.setOrderNo(order.getOrderNo().getValue());
        po.setCustomerId(order.getCustomerId());
        po.setShippingAddress(formatAddress(order.getShippingAddress()));
        po.setStatus(order.getStatus().getValue());
        po.setTotalAmount(order.getTotalAmount().getValue());
        po.setRemark(order.getRemark());
        po.setItems(formatOrderItems(order.getItems(), order.getId().getValue()));
        po.setCreatedAt(order.getCreatedAt());
        po.setUpdatedAt(order.getUpdatedAt());
        return po;
    }

    default Address parseAddress(String addressStr) {
        if (addressStr == null || addressStr.isEmpty()) return null;
        String[] parts = addressStr.split("\\|");
        if (parts.length >= 3) {
            return new Address(parts[0], parts[1], parts[2], parts.length > 3 ? parts[3] : null);
        }
        return new Address(addressStr, null, null, null);
    }

    default String formatAddress(Address address) {
        if (address == null) return null;
        return String.format("%s|%s|%s|%s",
                address.getProvince() != null ? address.getProvince() : "",
                address.getCity() != null ? address.getCity() : "",
                address.getDetail() != null ? address.getDetail() : "",
                address.getPostalCode() != null ? address.getPostalCode() : "");
    }

    default List<OrderItem> parseOrderItems(List<OrderItemPO> items) {
        if (items == null) return new ArrayList<>();
        return items.stream()
                .map(this::toOrderItem)
                .toList();
    }

    default OrderItem toOrderItem(OrderItemPO po) {
        OrderItem item = new OrderItem();
        item.setId(OrderItem.OrderItemId.of(po.getId()));
        item.setProductId(po.getProductId());
        item.setProductName(po.getProductName());
        item.setSpecification(po.getSpecification());
        item.setPrice(Money.of(po.getPrice()));
        item.setQuantity(Quantity.of(po.getQuantity()));
        item.setSubtotal(Money.of(po.getSubtotal()));
        return item;
    }

    default List<OrderItemPO> formatOrderItems(List<OrderItem> items, String orderId) {
        if (items == null) return new ArrayList<>();
        return items.stream()
                .map(item -> toOrderItemPO(item, orderId))
                .toList();
    }

    default OrderItemPO toOrderItemPO(OrderItem item, String orderId) {
        OrderItemPO po = new OrderItemPO();
        po.setId(item.getId().getValue());
        po.setOrderId(orderId);
        po.setProductId(item.getProductId());
        po.setProductName(item.getProductName());
        po.setSpecification(item.getSpecification());
        po.setPrice(item.getPrice().getValue());
        po.setQuantity(item.getQuantity().getValue());
        po.setSubtotal(item.getSubtotal().getValue());
        return po;
    }
}
