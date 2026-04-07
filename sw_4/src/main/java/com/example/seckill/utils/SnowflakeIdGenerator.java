package com.example.seckill.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 雪花算法ID生成器
 * 结构: 1位符号位 + 41位时间戳 + 10位机器ID + 12位序列号
 */
@Slf4j
@Component
public class SnowflakeIdGenerator {

    // 起始时间戳 (2024-01-01 00:00:00)
    private final long START_TIMESTAMP = 1704067200000L;

    // 各部分位数
    private final long DATA_CENTER_ID_BITS = 5L;  // 数据中心ID位数
    private final long WORKER_ID_BITS = 5L;       // 机器ID位数
    private final long SEQUENCE_BITS = 12L;       // 序列号位数

    // 最大值
    private final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 左移位数
    private final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    // 配置
    private long dataCenterId = 0L;
    private long workerId = 0L;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    @PostConstruct
    public void init() {
        try {
            // 根据IP地址生成workerId
            String ip = InetAddress.getLocalHost().getHostAddress();
            workerId = Math.abs(ip.hashCode()) % (MAX_WORKER_ID + 1);
            log.info("Snowflake initialized - DataCenterId: {}, WorkerId: {}", dataCenterId, workerId);
        } catch (UnknownHostException e) {
            workerId = 0L;
            log.warn("Failed to get IP, using default workerId: 0");
        }
    }

    /**
     * 生成唯一ID
     */
    public synchronized long nextId() {
        long currentTimestamp = getCurrentTimestamp();

        // 时钟回拨检查
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        // 同一毫秒内
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        // 组合ID
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 生成订单ID字符串
     */
    public String generateOrderId() {
        return "ORD" + nextId();
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

    /**
     * 从ID中提取时间戳
     */
    public long extractTimestamp(long id) {
        return (id >> TIMESTAMP_SHIFT) + START_TIMESTAMP;
    }

    /**
     * 从ID中提取数据中心ID
     */
    public long extractDataCenterId(long id) {
        return (id >> DATA_CENTER_ID_SHIFT) & MAX_DATA_CENTER_ID;
    }

    /**
     * 从ID中提取机器ID
     */
    public long extractWorkerId(long id) {
        return (id >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
    }

    /**
     * 从ID中提取序列号
     */
    public long extractSequence(long id) {
        return id & MAX_SEQUENCE;
    }
}
