package com.ghtransport.order.domain.vo;

import com.ghtransport.common.exception.DomainException;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Quantity {

    private final int value;

    public Quantity(int value) {
        if (value <= 0) {
            throw new DomainException("数量必须大于0");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public BigDecimal getValueAsBigDecimal() {
        return BigDecimal.valueOf(value);
    }
}
