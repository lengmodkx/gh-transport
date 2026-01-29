package com.ghtransport.common.redis.cache;

import com.ghtransport.common.core.result.ResultCode;
import com.ghtransport.common.core.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务
 */
@Slf4j
@Component
public class CacheService {

    private static final String CACHE_PREFIX = "cache:";
    private static final String LOCK_PREFIX = "lock:";

    private final StringRedisTemplate redisTemplate;

    public CacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== String类型操作 ====================

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        set(key, value, null);
    }

    /**
     * 设置缓存（带过期时间）
     *
     * @param key        键
     * @param value      值
     * @param expireTime 过期时间
     * @param timeUnit   时间单位
     */
    public void set(String key, Object value, Long expireTime, TimeUnit timeUnit) {
        String fullKey = CACHE_PREFIX + key;
        String jsonValue = JsonUtils.toJson(value);
        if (expireTime != null && expireTime > 0) {
            redisTemplate.opsForValue().set(fullKey, jsonValue, Duration.of(expireTime, timeUnit.toChronoUnit()));
        } else {
            redisTemplate.opsForValue().set(fullKey, jsonValue);
        }
    }

    /**
     * 设置缓存（带过期时间 Duration）
     */
    public void set(String key, Object value, Duration duration) {
        String fullKey = CACHE_PREFIX + key;
        String jsonValue = JsonUtils.toJson(value);
        redisTemplate.opsForValue().set(fullKey, jsonValue, duration);
    }

    /**
     * 获取缓存
     *
     * @param key   键
     * @param clazz 值类型
     * @return 值
     */
    public <T> T get(String key, Class<T> clazz) {
        String fullKey = CACHE_PREFIX + key;
        String value = redisTemplate.opsForValue().get(fullKey);
        if (value == null) {
            return null;
        }
        return JsonUtils.fromJson(value, clazz);
    }

    /**
     * 获取缓存（返回Optional）
     */
    public <T> Optional<T> getOptional(String key, Class<T> clazz) {
        T value = get(key, clazz);
        return Optional.ofNullable(value);
    }

    /**
     * 删除缓存
     *
     * @param key 键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        String fullKey = CACHE_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.delete(fullKey));
    }

    /**
     * 批量删除缓存
     *
     * @param keys 键列表
     * @return 删除数量
     */
    public Long delete(Collection<String> keys) {
        List<String> fullKeys = keys.stream()
                .map(k -> CACHE_PREFIX + k)
                .toList();
        return redisTemplate.delete(fullKeys);
    }

    // ==================== Hash类型操作 ====================

    /**
     * 设置Hash缓存
     *
     * @param key     键
     * @param hashKey Hash键
     * @param value   值
     */
    public void hSet(String key, String hashKey, Object value) {
        String fullKey = CACHE_PREFIX + key;
        String jsonValue = JsonUtils.toJson(value);
        redisTemplate.opsForHash().put(fullKey, hashKey, jsonValue);
    }

    /**
     * 获取Hash缓存
     *
     * @param key     键
     * @param hashKey Hash键
     * @param clazz   值类型
     * @return 值
     */
    public <T> T hGet(String key, String hashKey, Class<T> clazz) {
        String fullKey = CACHE_PREFIX + key;
        Object value = redisTemplate.opsForHash().get(fullKey, hashKey);
        if (value == null) {
            return null;
        }
        return JsonUtils.fromJson(value.toString(), clazz);
    }

    /**
     * 获取Hash所有值
     *
     * @param key 键
     * @param clazz 值类型
     * @return 值列表
     */
    public <T> List<T> hGetAll(String key, Class<T> clazz) {
        String fullKey = CACHE_PREFIX + key;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(fullKey);
        if (entries.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        for (Object value : entries.values()) {
            result.add(JsonUtils.fromJson(value.toString(), clazz));
        }
        return result;
    }

    /**
     * 删除Hash缓存
     *
     * @param key     键
     * @param hashKeys Hash键列表
     * @return 删除数量
     */
    public Long hDelete(String key, String... hashKeys) {
        String fullKey = CACHE_PREFIX + key;
        return redisTemplate.opsForHash().delete(fullKey, (Object[]) hashKeys);
    }

    // ==================== List类型操作 ====================

    /**
     * 向List左侧添加元素
     *
     * @param key   键
     * @param values 值列表
     * @return List长度
     */
    public Long lPush(String key, Object... values) {
        String fullKey = CACHE_PREFIX + key;
        String[] jsonValues = Arrays.stream(values)
                .map(JsonUtils::toJson)
                .toArray(String[]::new);
        return redisTemplate.opsForList().leftPushAll(fullKey, jsonValues);
    }

    /**
     * 向List右侧添加元素
     *
     * @param key   键
     * @param values 值列表
     * @return List长度
     */
    public Long rPush(String key, Object... values) {
        String fullKey = CACHE_PREFIX + key;
        String[] jsonValues = Arrays.stream(values)
                .map(JsonUtils::toJson)
                .toArray(String[]::new);
        return redisTemplate.opsForList().rightPushAll(fullKey, jsonValues);
    }

    /**
     * 获取List范围
     *
     * @param key   键
     * @param start 起始索引
     * @param end   结束索引
     * @param clazz 值类型
     * @return 值列表
     */
    public <T> List<T> lRange(String key, long start, long end, Class<T> clazz) {
        String fullKey = CACHE_PREFIX + key;
        List<String> values = redisTemplate.opsForList().range(fullKey, start, end);
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream()
                .map(v -> JsonUtils.fromJson(v, clazz))
                .toList();
    }

    // ==================== Set类型操作 ====================

    /**
     * 向Set添加元素
     *
     * @param key    键
     * @param values 值列表
     * @return 添加数量
     */
    public Long sAdd(String key, Object... values) {
        String fullKey = CACHE_PREFIX + key;
        String[] jsonValues = Arrays.stream(values)
                .map(JsonUtils::toJson)
                .toArray(String[]::new);
        return redisTemplate.opsForSet().add(fullKey, jsonValues);
    }

    /**
     * 获取Set所有元素
     *
     * @param key  键
     * @param clazz 值类型
     * @return 值集合
     */
    public <T> Set<T> sMembers(String key, Class<T> clazz) {
        String fullKey = CACHE_PREFIX + key;
        Set<String> values = redisTemplate.opsForSet().members(fullKey);
        if (values == null || values.isEmpty()) {
            return Collections.emptySet();
        }
        Set<T> result = new HashSet<>();
        for (String value : values) {
            result.add(JsonUtils.fromJson(value, clazz));
        }
        return result;
    }

    /**
     * 判断Set是否包含元素
     *
     * @param key   键
     * @param value 值
     * @return 是否包含
     */
    public boolean sIsMember(String key, Object value) {
        String fullKey = CACHE_PREFIX + key;
        String jsonValue = JsonUtils.toJson(value);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(fullKey, jsonValue));
    }

    // ==================== 过期操作 ====================

    /**
     * 设置过期时间
     *
     * @param key        键
     * @param expireTime 过期时间
     * @param timeUnit   时间单位
     * @return 是否设置成功
     */
    public boolean expire(String key, long expireTime, TimeUnit timeUnit) {
        String fullKey = CACHE_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.expire(fullKey, Duration.of(expireTime, timeUnit.toChronoUnit())));
    }

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间（秒），-1表示永不过期，-2表示键不存在
     */
    public Long getExpire(String key) {
        String fullKey = CACHE_PREFIX + key;
        return redisTemplate.getExpire(fullKey, TimeUnit.SECONDS);
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean exists(String key) {
        String fullKey = CACHE_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(fullKey));
    }

    // ==================== 工具方法 ====================

    /**
     * 获取完整的缓存键
     */
    public String getFullKey(String key) {
        return CACHE_PREFIX + key;
    }

    /**
     * 根据前缀模糊查找键
     *
     * @param prefix 前缀
     * @return 键集合
     */
    public Set<String> keys(String prefix) {
        String fullPrefix = CACHE_PREFIX + prefix + "*";
        Set<String> keys = redisTemplate.keys(fullPrefix);
        if (keys == null || keys.isEmpty()) {
            return Collections.emptySet();
        }
        // 去掉前缀
        Set<String> result = new HashSet<>();
        for (String key : keys) {
            result.add(key.substring(CACHE_PREFIX.length()));
        }
        return result;
    }

    /**
     * 根据前缀模糊删除键
     *
     * @param prefix 前缀
     * @return 删除数量
     */
    public Long deleteByPrefix(String prefix) {
        Set<String> keys = keys(prefix);
        if (keys.isEmpty()) {
            return 0L;
        }
        return delete(keys);
    }
}
