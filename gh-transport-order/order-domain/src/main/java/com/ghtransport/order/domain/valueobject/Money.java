package com.ghtransport.order.domain.vo;

import com.ghtransport.common.domain.ValueObject;
import com.ghtransport.common.util.Validate;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Currency;

@Getter
@EqualsAndHashCode
public class Money implements ValueObject {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount) {
        this(amount, Currency.CNY);
    }

    public Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new DomainException("金额不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("金额不能为负数");
        }
        this.amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
        this.currency = currency != null ? currency : Currency.CNY;
    }

    public Money add(Money other) {
        checkCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    private void checkCurrency(Money other) {
        if (this.currency != other.currency) {
            throw new DomainException("货币类型不一致");
        }
    }
}
