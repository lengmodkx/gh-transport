package com.ghtransport.common.core.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON工具类 - 基于FastJSON2
 */
public class JsonUtils {

    private JsonUtils() {
    }

    /**
     * 对象转JSON字符串
     */
    public static String toJson(Object object) {
        return JSON.toJSONString(object);
    }

    /**
     * 对象转JSON字符串（格式化）
     */
    public static String toJsonPretty(Object object) {
        return JSON.toJSONString(object, true);
    }

    /**
     * JSON字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return JSON.parseObject(json, clazz);
    }

    /**
     * JSON字符串转List
     */
    public static <T> List<T> parseList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return JSON.parseArray(json, clazz);
    }

    /**
     * JSON字符串转Set
     */
    public static <T> Set<T> parseSet(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        List<T> list = parseList(json, clazz);
        return list != null ? Set.copyOf(list) : null;
    }

    /**
     * JSON字符串转Map
     */
    public static <K, V> Map<K, V> parseMap(String json, Class<K> keyClass, Class<V> valueClass) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return JSON.parseObject(json, new TypeReference<Map<K, V>>(keyClass, valueClass) {});
    }

    /**
     * JSON字符串转复杂对象
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return JSON.parseObject(json, typeReference);
    }

    /**
     * 判断是否为有效JSON
     */
    public static boolean isJson(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            JSON.parse(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取JSON节点
     */
    public static Object getNode(String json, String path) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return JSONPath.of(path).extract(json);
    }
}
