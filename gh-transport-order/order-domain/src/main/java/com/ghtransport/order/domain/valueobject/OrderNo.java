package com.ghtransport.order.domain.vo;

import com.ghtransport.common.exception.DomainException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class OrderNo {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private final String value;

    private OrderNo(String value) {
        this.value = value;
    }

    public static OrderNo generate() {
        String prefix = LocalDateTime.now().format(FORMATTER);
        int suffix = RANDOM.nextInt(1000, 10000);
        return new OrderNo(prefix + suffix);
    }

    public static OrderNo of(String value) {
        if (value == null || value.length() != 18) {
            throw new DomainException("订单号格式不正确");
        }
        return new OrderNo(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
