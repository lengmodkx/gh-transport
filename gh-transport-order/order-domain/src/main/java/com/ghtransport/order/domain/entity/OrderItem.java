package com.ghtransport.order.domain.entity;

import com.ghtransport.common.util.Validate;
import com.ghtransport.order.domain.vo.Money;
import com.ghtransport.order.domain.vo.Quantity;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class OrderItem {

    private final String id;
    private final String itemName;
    private final Quantity quantity;
    private final BigDecimal weight;
    private final BigDecimal volume;
    private final Money unitPrice;
    private final Money subtotal;

    private OrderItem(String id, String itemName, Quantity quantity,
                      BigDecimal weight, BigDecimal volume, Money unitPrice) {
        Validate.notBlank(itemName, "货物名称不能为空");
        Validate.notNull(quantity, "货物数量不能为空");
        Validate.notNull(unitPrice, "单价不能为空");

        this.id = id;
        this.itemName = itemName;
        this.quantity = quantity;
        this.weight = weight;
        this.volume = volume;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(quantity.getValue());
    }

    public static OrderItem create(String itemName, Quantity quantity,
                                    BigDecimal weight, BigDecimal volume,
                                    Money unitPrice) {
        return new OrderItem(
            UUID.randomUUID().toString(),
            itemName,
            quantity,
            weight,
            volume,
            unitPrice
        );
    }
}
