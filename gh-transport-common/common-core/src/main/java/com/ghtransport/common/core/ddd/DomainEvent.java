package com.ghtransport.common.core.ddd;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 领域事件基类
 *
 * 领域事件的特点：
 * 1. 表示领域中发生的事实
 * 2. 应该是不变的
 * 3. 可以被监听和处理
 * 4. 通常在聚合状态变化后发布
 */
@Data
@Slf4j
public abstract class DomainEvent {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 聚合根类型
     */
    private String aggregateType;

    /**
     * 聚合根ID
     */
    private String aggregateId;

    /**
     * 事件发生时间
     */
    private LocalDateTime occurredOn;

    /**
     * 事件发布时间
     */
    private LocalDateTime publishedOn;

    /**
     * 是否已发布
     */
    private boolean published;

    /**
     * 事件版本
     */
    private int version;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.published = false;
        this.version = 1;
    }

    protected DomainEvent(String aggregateId) {
        this();
        this.aggregateId = aggregateId;
    }

    /**
     * 获取事件类型名称
     */
    public String getEventType() {
        return getClass().getSimpleName();
    }

    /**
     * 标记为已发布
     */
    public void markAsPublished() {
        this.published = true;
        this.publishedOn = LocalDateTime.now();
    }

    /**
     * 判断是否已发布
     */
    public boolean isPublished() {
        return published;
    }

    /**
     * 获取事件的业务描述
     */
    public abstract String getBusinessDescription();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DomainEvent that = (DomainEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return String.format("%s[eventId=%s, aggregateType=%s, aggregateId=%s, occurredOn=%s]",
                getEventType(), eventId, aggregateType, aggregateId, occurredOn);
    }
}
