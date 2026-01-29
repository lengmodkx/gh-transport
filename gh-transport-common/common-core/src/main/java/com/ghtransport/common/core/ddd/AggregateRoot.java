package com.ghtransport.common.core.ddd;

import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 聚合根基类
 *
 * @param <ID> ID类型
 */
@Getter
public abstract class AggregateRoot<ID> implements Entity<ID> {

    private static final long serialVersionUID = 1L;

    /**
     * 聚合根ID（不可变）
     */
    private final ID id;

    /**
     * 版本号（用于乐观锁）
     */
    private Long version;

    /**
     * 领域事件列表
     */
    private final transient List<DomainEvent> domainEvents;

    /**
     * 领域事件发布者（通过构造函数注入）
     */
    private final transient ApplicationEventPublisher eventPublisher;

    protected AggregateRoot() {
        this(null, null);
    }

    protected AggregateRoot(ID id) {
        this(id, null);
    }

    protected AggregateRoot(ID id, ApplicationEventPublisher eventPublisher) {
        this.id = id;
        this.domainEvents = new ArrayList<>();
        this.eventPublisher = eventPublisher;
    }

    /**
     * 获取聚合根类型
     */
    public abstract String getAggregateType();

    /**
     * 注册领域事件
     *
     * @param event 领域事件
     */
    protected void registerEvent(DomainEvent event) {
        if (event == null) {
            return;
        }
        event.setAggregateId(id != null ? id.toString() : null);
        event.setAggregateType(getAggregateType());
        event.setOccurredOn(LocalDateTime.now());
        domainEvents.add(event);
    }

    /**
     * 清除所有领域事件
     */
    public void clearEvents() {
        domainEvents.clear();
    }

    /**
     * 获取所有领域事件
     */
    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    /**
     * 获取未发布的领域事件
     */
    public List<DomainEvent> getUnpublishedEvents() {
        return domainEvents.stream()
                .filter(e -> !e.isPublished())
                .toList();
    }

    /**
     * 标记事件为已发布
     */
    public void markEventsAsPublished() {
        domainEvents.forEach(DomainEvent::markAsPublished);
    }

    /**
     * 发布领域事件（同步）
     */
    protected void publishEvent(DomainEvent event) {
        if (eventPublisher != null && event != null) {
            eventPublisher.publishEvent(event);
            event.markAsPublished();
        }
    }

    /**
     * 发布所有领域事件
     */
    public void publishAllEvents() {
        List<DomainEvent> events = getUnpublishedEvents();
        for (DomainEvent event : events) {
            publishEvent(event);
        }
    }

    /**
     * 设置领域事件发布者（兼容旧版本，优先使用构造注入）
     */
    public static void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        // 静态方法仅用于兼容，实际使用构造注入
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AggregateRoot<?> that = (AggregateRoot<?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s]", getClass().getSimpleName(), id);
    }
}
