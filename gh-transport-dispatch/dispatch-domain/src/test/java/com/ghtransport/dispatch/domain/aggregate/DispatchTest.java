package com.ghtransport.dispatch.domain.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 调度单聚合根测试
 */
class DispatchTest {

    @Test
    @DisplayName("创建调度单 - 成功")
    void createDispatch_Success() {
        // Given & When
        Dispatch dispatch = Dispatch.create(
            "order-001",
            "vehicle-001",
            "driver-001",
            LocalDateTime.now().plusHours(2),
            "北京市朝阳区",
            "上海市浦东新区"
        );

        // Then
        assertNotNull(dispatch.getId());
        assertNotNull(dispatch.getDispatchNo());
        assertTrue(dispatch.getDispatchNo().getValue().startsWith("DSP"));
        assertEquals("order-001", dispatch.getOrderId());
        assertEquals(Dispatch.DispatchStatus.PENDING, dispatch.getStatus());
    }

    @Test
    @DisplayName("分配调度单")
    void assignDispatch() {
        // Given
        Dispatch dispatch = Dispatch.create(
            "order-001", "vehicle-001", "driver-001",
            LocalDateTime.now(), "起点", "终点"
        );

        // When
        dispatch.assign();

        // Then
        assertEquals(Dispatch.DispatchStatus.ASSIGNED, dispatch.getStatus());
    }

    @Test
    @DisplayName("开始运输")
    void startTransport() {
        // Given
        Dispatch dispatch = Dispatch.create(
            "order-001", "vehicle-001", "driver-001",
            LocalDateTime.now(), "起点", "终点"
        );
        dispatch.assign();

        // When
        dispatch.start();

        // Then
        assertEquals(Dispatch.DispatchStatus.IN_TRANSIT, dispatch.getStatus());
    }

    @Test
    @DisplayName("完成调度")
    void completeDispatch() {
        // Given
        Dispatch dispatch = Dispatch.create(
            "order-001", "vehicle-001", "driver-001",
            LocalDateTime.now(), "起点", "终点"
        );
        dispatch.assign();
        dispatch.start();

        // When
        dispatch.complete();

        // Then
        assertEquals(Dispatch.DispatchStatus.COMPLETED, dispatch.getStatus());
        assertNotNull(dispatch.getActualTime());
    }

    @Test
    @DisplayName("调度单状态流转")
    void dispatchStatusTransition() {
        // Given
        Dispatch dispatch = Dispatch.create(
            "order-001", "vehicle-001", "driver-001",
            LocalDateTime.now(), "起点", "终点"
        );

        // PENDING -> ASSIGNED
        assertEquals(Dispatch.DispatchStatus.PENDING, dispatch.getStatus());
        dispatch.assign();
        assertEquals(Dispatch.DispatchStatus.ASSIGNED, dispatch.getStatus());

        // ASSIGNED -> IN_TRANSIT
        dispatch.start();
        assertEquals(Dispatch.DispatchStatus.IN_TRANSIT, dispatch.getStatus());

        // IN_TRANSIT -> COMPLETED
        dispatch.complete();
        assertEquals(Dispatch.DispatchStatus.COMPLETED, dispatch.getStatus());
    }
}
