package com.ghtransport.order.domain.vo;

import com.ghtransport.common.exception.DomainException;
import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("待调度") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == DISPATCHED || newStatus == CANCELLED;
        }
    },
    DISPATCHED("已调度") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == IN_TRANSIT || newStatus == CANCELLED;
        }
    },
    IN_TRANSIT("运输中") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == DELIVERED || newStatus == EXCEPTION;
        }
    },
    DELIVERED("已送达") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == COMPLETED || newStatus == EXCEPTION;
        }
    },
    COMPLETED("已完成") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return false;
        }
    },
    CANCELLED("已取消") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return false;
        }
    },
    EXCEPTION("异常") {
        @Override
        public boolean canTransitionTo(OrderStatus newStatus) {
            return newStatus == IN_TRANSIT || newStatus == COMPLETED;
        }
    };

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public abstract boolean canTransitionTo(OrderStatus newStatus);

    public void validateTransition(OrderStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new DomainException(
                String.format("订单状态不能从 %s 变更为 %s", this.description, newStatus.description)
            );
        }
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isDispatched() {
        return this == DISPATCHED;
    }

    public boolean isInTransit() {
        return this == IN_TRANSIT;
    }

    public boolean isDelivered() {
        return this == DELIVERED;
    }
}
