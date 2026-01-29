package com.ghtransport.common.redis.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 */
@Slf4j
@Component
public class DistributedLock {

    private static final String LOCK_PREFIX = "lock:";
    private static final String LOCK_SUCCESS = "OK";
    private static final Long RELEASE_SUCCESS = 1L;

    private final StringRedisTemplate redisTemplate;

    /**
     * 解锁脚本
     */
    private static final RedisScript<Long> UNLOCK_SCRIPT = RedisScript.of(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else " +
                    "return 0 " +
                    "end",
            Long.class
    );

    public DistributedLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取锁
     *
     * @param lockKey    锁键
     * @param lockValue  锁值（通常是线程ID或唯一标识）
     * @param expireTime 过期时间
     * @param timeUnit   时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String lockValue, long expireTime, TimeUnit timeUnit) {
        String key = LOCK_PREFIX + lockKey;
        String result = redisTemplate.opsForValue().setIfAbsent(
                key,
                lockValue,
                Duration.of(expireTime, timeUnit.toChronoUnit())
        );
        return LOCK_SUCCESS.equals(result);
    }

    /**
     * 尝试获取锁（默认过期时间30秒）
     *
     * @param lockKey   锁键
     * @param lockValue 锁值
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String lockValue) {
        return tryLock(lockKey, lockValue, 30, TimeUnit.SECONDS);
    }

    /**
     * 尝试获取锁（带等待时间）
     *
     * @param lockKey     锁键
     * @param lockValue   锁值
     * @param waitTime    等待时间
     * @param expireTime  过期时间
     * @param timeUnit    时间单位
     * @return 是否获取成功
     */
    public boolean tryLockWithWaitTime(String lockKey, String lockValue,
                                        long waitTime, long expireTime, TimeUnit timeUnit) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeUnit.toMillis(waitTime)) {
            if (tryLock(lockKey, lockValue, expireTime, timeUnit)) {
                return true;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * 释放锁
     *
     * @param lockKey   锁键
     * @param lockValue 锁值
     * @return 是否释放成功
     */
    public boolean unlock(String lockKey, String lockValue) {
        String key = LOCK_PREFIX + lockKey;
        Long result = redisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(key),
                lockValue
        );
        return RELEASE_SUCCESS.equals(result);
    }

    /**
     * 释放锁（使用Lua脚本，保证原子性）
     *
     * @param lockKey   锁键
     * @param lockValue 锁值
     * @return 是否释放成功
     */
    public boolean unlockWithScript(String lockKey, String lockValue) {
        return unlock(lockKey, lockValue);
    }

    /**
     * 强制释放锁（不考虑锁值）
     *
     * @param lockKey 锁键
     */
    public void forceUnlock(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        redisTemplate.delete(key);
    }

    /**
     * 延长锁的过期时间
     *
     * @param lockKey    锁键
     * @param lockValue  锁值
     * @param expireTime 新的过期时间
     * @param timeUnit   时间单位
     * @return 是否延长成功
     */
    public boolean extendLock(String lockKey, String lockValue, long expireTime, TimeUnit timeUnit) {
        String key = LOCK_PREFIX + lockKey;
        String currentValue = redisTemplate.opsForValue().get(key);
        if (lockValue.equals(currentValue)) {
            return Boolean.TRUE.equals(
                    redisTemplate.expire(key, Duration.of(expireTime, timeUnit.toChronoUnit()))
            );
        }
        return false;
    }

    /**
     * 获取锁的剩余时间
     *
     * @param lockKey 锁键
     * @return 剩余时间（毫秒），-1表示永不过期，-2表示锁不存在
     */
    public long getLockTTL(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        return ttl != null ? ttl : -2;
    }
}
