package com.ghtransport.common.core.ddd;

/**
 * 领域事件发布者接口
 *
 * 用于发布领域事件，支持同步和异步发布
 */
public interface DomainEventPublisher {

    /**
     * 发布领域事件
     *
     * @param event 领域事件
     */
    void publish(DomainEvent event);

    /**
     * 发布多个领域事件
     *
     * @param events 领域事件列表
     */
    default void publishAll(Iterable<DomainEvent> events) {
        for (DomainEvent event : events) {
            publish(event);
        }
    }

    /**
     * 判断是否支持异步发布
     */
    default boolean isAsync() {
        return false;
    }
}
