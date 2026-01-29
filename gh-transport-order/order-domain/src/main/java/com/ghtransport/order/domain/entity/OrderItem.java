package com.ghtransport.order.domain.entity;

import com.ghtransport.common.core.ddd.Entity;
import com.ghtransport.common.core.ddd.ValueObject;
import com.ghtransport.common.core.util.IdGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends Entity<OrderItemId> {

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品规格
     */
    private String specification;

    /**
     * 单价
     */
    private Money price;

    /**
     * 数量
     */
    private Quantity quantity;

    /**
     * 小计金额
     */
    private Money subtotal;

    public OrderItem() {
        super();
    }

    public OrderItem(String productId, String productName, String specification,
                     Money price, Quantity quantity) {
        super(OrderItemId.generate());
        this.productId = productId;
        this.productName = productName;
        this.specification = specification;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = price.multiply(quantity.getValue());
    }

    /**
     * 商品ID
     */
    @Data
    @ValueObject
    public static class OrderItemId {
        private final String value;

        public static OrderItemId of(String value) {
            return new OrderItemId(value);
        }

        public static OrderItemId generate() {
            return new OrderItemId(IdGenerator.generateUUID());
        }
    }

    /**
     * 金额值对象
     */
    @Data
    @ValueObject
    public static class Money {
        private final BigDecimal value;

        public static Money of(BigDecimal value) {
            if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("金额不能为负数");
            }
            return new Money(value.setScale(2, java.math.RoundingMode.HALF_UP));
        }

        public static Money of(String value) {
            return Money.of(new BigDecimal(value));
        }

        public Money multiply(int factor) {
            return Money.of(this.value.multiply(BigDecimal.valueOf(factor)));
        }

        public Money add(Money other) {
            return Money.of(this.value.add(other.value));
        }
    }

    /**
     * 数量值对象
     */
    @Data
    @ValueObject
    public static class Quantity {
        private final int value;

        public static Quantity of(int value) {
            if (value <= 0) {
                throw new IllegalArgumentException("数量必须大于0");
            }
            return new Quantity(value);
        }
    }
}
