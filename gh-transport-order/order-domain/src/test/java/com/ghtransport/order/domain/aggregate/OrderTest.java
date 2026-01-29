package com.ghtransport.order.domain.aggregate;

import com.ghtransport.common.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单聚合根测试
 */
class OrderTest {

    private OrderItem createOrderItem(String productId, String productName, String price, int quantity) {
        return new OrderItem(
            productId,
            productName,
            "规格A",
            OrderItem.Money.of(price),
            OrderItem.Quantity.of(quantity)
        );
    }

    @Test
    @DisplayName("创建订单 - 成功")
    void createOrder_Success() {
        // Given
        String customerId = "customer-001";
        Address address = Address.of(
            "北京市", "北京市", "朝阳区", "xxx路1号",
            "张三", "13812345678", "100000"
        );
        List<OrderItem> items = List.of(
            createOrderItem("prod-001", "商品A", "99.99", 2),
            createOrderItem("prod-002", "商品B", "199.99", 1)
        );

        // When
        Order order = Order.create(customerId, address, items, "测试订单");

        // Then
        assertNotNull(order.getId());
        assertNotNull(order.getOrderNo());
        assertTrue(order.getOrderNo().getValue().startsWith("ORD"));
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("399.97"), order.getTotalAmount().getValue());
    }

    @Test
    @DisplayName("创建订单 - 计算总金额正确")
    void createOrder_CalculateTotalAmount() {
        // Given
        Address address = Address.of("北京", "北京", "朝阳", "xxx", "张", "138", "100");
        List<OrderItem> items = List.of(
            createOrderItem("p1", "商品1", "100.00", 3),  // 300
            createOrderItem("p2", "商品2", "50.00", 4)    // 200
        );

        // When
        Order order = Order.create("customer-001", address, items, null);

        // Then
        assertEquals(new BigDecimal("500.00"), order.getTotalAmount().getValue());
    }

    @Test
    @DisplayName("确认订单 - 待确认状态成功")
    void confirmOrder_Success() {
        // Given
        Order order = Order.create(
            "customer-001",
            Address.of("北京", "北京", "朝阳", "xxx", "张", "138", "100"),
            List.of(createOrderItem("p1", "商品1", "100.00", 1)),
            null
        );

        // When
        order.confirm();

        // Then
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertNotNull(order.getDomainEvents());
    }

    @Test
    @DisplayName("确认订单 - 非待确认状态失败")
    void confirmOrder_InvalidStatus() {
        // Given
        Order order = Order.create(
            "customer-001",
            Address.of("北京", "北京", "朝阳", "xxx", "张", "138", "100"),
            List.of(createOrderItem("p1", "商品1", "100.00", 1)),
            null
        );
        order.confirm();

        // When & Then - 不能再次确认
        assertThrows(BusinessException.class, order::confirm);
    }

    @Test
    @DisplayName("取消订单 - 待确认状态成功")
    void cancelOrder_Pending_Success() {
        // Given
        Order order = Order.create(
            "customer-001",
            Address.of("北京", "北京", "朝阳", "xxx", "张", "138", "100"),
            List.of(createOrderItem("p1", "商品1", "100.00", 1)),
            null
        );

        // When
        order.cancel("不想要了");

        // Then
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("取消订单 - 已发货状态失败")
    void cancelOrder_Shipped_Fail() {
        // Given
        Order order = Order.create(
            "customer-001",
            Address.of("北京", "北京", "朝阳", "xxx", "张", "138", "100"),
            List.of(createOrderItem("p1", "商品1", "100.00", 1)),
            null
        );
        order.confirm();
        order.ship("tracking-001");

        // When & Then
        assertThrows(BusinessException.class, () -> order.cancel("不想要了"));
    }

    @Test
    @DisplayName("发货 - 确认状态成功")
    void shipOrder_Confirmed_Success() {
        // Given
        Order order = Order.create(
            "customer-001",
            Address.of("北京", "北京", "朝阳", "xxx", "张", "138", "100"),
            List.of(createOrderItem("p1", "商品1", "100.00", 1)),
            null
        );
        order.confirm();

        // When
        order.ship("tracking-001");

        // Then
        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    @DisplayName("完成订单 - 发货状态成功")
    void completeOrder_Shipped_Success() {
        // Given
        Order order = Order.create(
            "customer-001",
            Address.of("北京", "北京", "朝阳", "xxx", "张", "138", "100"),
            List.of(createOrderItem("p1", "商品1", "100.00", 1)),
            null
        );
        order.confirm();
        order.ship("tracking-001");

        // When
        order.complete();

        // Then
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    @DisplayName("订单状态流转")
    void orderStatusTransition() {
        // Given
        Order order = Order.create(
            "customer-001",
            Address.of("北京", "北京", "朝阳", "xxx", "张", "138", "100"),
            List.of(createOrderItem("p1", "商品1", "100.00", 1)),
            null
        );

        // Then - PENDING -> CONFIRMED
        assertEquals(OrderStatus.PENDING, order.getStatus());
        order.confirm();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        // CONFIRMED -> SHIPPED
        order.ship("TN001");
        assertEquals(OrderStatus.SHIPPED, order.getStatus());

        // SHIPPED -> COMPLETED
        order.complete();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }
}
