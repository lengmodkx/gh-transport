package com.ghtransport.common.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ID生成器测试
 */
class IdGeneratorTest {

    @Test
    @DisplayName("生成UUID")
    void generateUUID() {
        // When
        String uuid = IdGenerator.generateUUID();

        // Then
        assertNotNull(uuid);
        assertEquals(32, uuid.length());
        assertTrue(uuid.matches("[a-f0-9]+"));
    }

    @Test
    @DisplayName("生成订单号")
    void generateOrderNo() {
        // When
        String orderNo = IdGenerator.generateOrderNo();

        // Then
        assertNotNull(orderNo);
        assertTrue(orderNo.startsWith("ORD"));
        assertEquals(22, orderNo.length());  // ORD + 8位日期 + 8位流水号
    }

    @Test
    @DisplayName("生成运单号")
    void generateWaybillNo() {
        // When
        String waybillNo = IdGenerator.generateWaybillNo();

        // Then
        assertNotNull(waybillNo);
        assertTrue(waybillNo.startsWith("WB"));
        assertEquals(22, waybillNo.length());
    }

    @Test
    @DisplayName("生成调度单号")
    void generateDispatchNo() {
        // When
        String dispatchNo = IdGenerator.generateDispatchNo();

        // Then
        assertNotNull(dispatchNo);
        assertTrue(dispatchNo.startsWith("DSP"));
        assertEquals(22, dispatchNo.length());
    }

    @Test
    @DisplayName("生成业务编号")
    void generateBizCode() {
        // When
        String code = IdGenerator.generateBizCode("TEST");

        // Then
        assertNotNull(code);
        assertTrue(code.startsWith("TEST"));
        assertEquals(20, code.length());  // TEST + 8位日期 + 8位流水号
    }

    @Test
    @DisplayName("生成雪花算法ID")
    void nextSnowflakeId() {
        // When
        long id1 = IdGenerator.nextSnowflakeId();
        long id2 = IdGenerator.nextSnowflakeId();

        // Then
        assertTrue(id1 > 0);
        assertTrue(id2 > 0);
        assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("批量生成ID唯一性")
    void batchGenerateIds_Unique() {
        // When
        String[] ids = new String[100];
        for (int i = 0; i < 100; i++) {
            ids[i] = IdGenerator.generateUUID();
        }

        // Then
        assertEquals(100, java.util.Arrays.stream(ids).distinct().count());
    }
}
