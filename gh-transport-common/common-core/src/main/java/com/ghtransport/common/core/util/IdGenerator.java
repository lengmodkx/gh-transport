package com.ghtransport.common.core.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ID生成器
 */
public class IdGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private IdGenerator() {
    }

    /**
     * 生成UUID（无连字符）
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成UUID v7格式（带时间戳）
     * 格式：时间戳(32bits) + 版本(4bits) + 变体(2bits) + 随机(26bits)
     */
    public static String generateUUIDv7() {
        long timestamp = System.currentTimeMillis();

        // 构建时间戳部分 (32 bits)
        long timeHi = (timestamp << 32) >>> 32;

        // 版本号 v7 (4 bits)
        int version = 7;

        // 变体位 (2 bits)
        int variantBits = 0x8000;

        // 随机部分 (62 bits)
        long clockSeqHiAndLow = RANDOM.nextLong();
        clockSeqHiAndLow &= 0x3FFF; // 14 bits
        clockSeqHiAndLow |= 0x8000; // 设置变体位

        // 组合
        long mostSigBits = (timestamp & 0xFFFFFFFFFFFF00000L) |
                          ((variantBits << 48) & 0xFFFF000000000000L) |
                          ((version << 52) & 0x000F000000000000L);

        long leastSigBits = (clockSeqHiAndLow << 48) |
                           (RANDOM.nextLong() & 0x0000FFFFFFFFFFFFL);

        return new UUID(mostSigBits, leastSigBits).toString();
    }

    /**
     * 雪花算法ID生成
     */
    public static synchronized long nextSnowflakeId() {
        return SnowflakeIdGenerator.nextId();
    }

    /**
     * 生成业务编号
     * 格式：前缀 + 日期 + 流水号（8位补零）
     */
    public static String generateBizCode(String prefix) {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        long sequence = nextSnowflakeId() & 0xFFFFFFFFL;
        return prefix + date + String.format("%08d", sequence);
    }

    /**
     * 生成订单号
     */
    public static String generateOrderNo() {
        return generateBizCode("ORD");
    }

    /**
     * 生成运单号
     */
    public static String generateWaybillNo() {
        return generateBizCode("WB");
    }

    /**
     * 生成调度单号
     */
    public static String generateDispatchNo() {
        return generateBizCode("DSP");
    }

    /**
     * 雪花算法内部类
     */
    private static class SnowflakeIdGenerator {
        private static final long WORKER_ID_BITS = 5L;
        private static final long DATACENTER_ID_BITS = 5L;
        private static final long SEQUENCE_BITS = 12L;

        private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
        private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
        private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

        private static long workerId = 1L;
        private static long datacenterId = 1L;
        private static long sequence = 0L;
        private static long lastTimestamp = -1L;

        public static synchronized long nextId() {
            long timestamp = System.currentTimeMillis();

            if (timestamp < lastTimestamp) {
                throw new RuntimeException("时钟回拨，拒绝生成ID");
            }

            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & ((1 << SEQUENCE_BITS) - 1);
                if (sequence == 0) {
                    while (timestamp <= lastTimestamp) {
                        timestamp = System.currentTimeMillis();
                    }
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            return (timestamp << TIMESTAMP_LEFT_SHIFT)
                  | (datacenterId << DATACENTER_ID_SHIFT)
                  | (workerId << WORKER_ID_SHIFT)
                  | sequence;
        }
    }
}
