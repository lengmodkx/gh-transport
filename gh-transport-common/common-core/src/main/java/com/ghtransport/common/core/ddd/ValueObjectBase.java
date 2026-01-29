package com.ghtransport.common.core.ddd;

import java.io.Serializable;
import java.util.*;

/**
 * 值对象基类
 *
 * 值对象的特点：
 * 1. 通过属性值来识别，属性相同则对象相同
 * 2. 不可变（创建后状态不变）
 * 3. 无身份标识（没有ID）
 * 4. 可以组合其他值对象
 *
 * @param <T> 值对象类型
 */
public abstract class ValueObject<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 比较属性值是否相等
     *
     * @param other 另一个值对象
     * @return 是否相等
     */
    public abstract boolean sameValueAs(T other);

    /**
     * 获取属性值的迭代器
     *
     * @return 属性值迭代器
     */
    protected abstract Iterator<Object> getValueIterator();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValueObject<?> that = (ValueObject<?>) o;
        return sameValueAs((T) that);
    }

    @Override
    public int hashCode() {
        return getValueStream()
                .map(obj -> {
                    if (obj == null) {
                        return 0;
                    }
                    if (obj instanceof byte[]) {
                        return Arrays.hashCode((byte[]) obj);
                    }
                    if (obj instanceof int[]) {
                        return Arrays.hashCode((int[]) obj);
                    }
                    if (obj instanceof long[]) {
                        return Arrays.hashCode((long[]) obj);
                    }
                    if (obj instanceof double[]) {
                        return Arrays.hashCode((double[]) obj);
                    }
                    if (obj instanceof float[]) {
                        return Arrays.hashCode((float[]) obj);
                    }
                    if (obj instanceof Object[]) {
                        return Arrays.hashCode((Object[]) obj);
                    }
                    return obj.hashCode();
                })
                .reduce(1, (a, b) -> 31 * a + b);
    }

    /**
     * 获取属性值的流
     *
     * @return 属性值流
     */
    protected java.util.stream.Stream<Object> getValueStream() {
        Iterator<Object> iterator = getValueIterator();
        List<Object> list = new ArrayList<>();
        while (iterator.hasNext()) {
            Object value = iterator.next();
            if (value instanceof Collection) {
                list.addAll((Collection<?>) value);
            } else if (value instanceof Map) {
                list.addAll(((Map<?, ?>) value).entrySet());
            } else {
                list.add(value);
            }
        }
        return list.stream();
    }

    /**
     * 创建值对象（工厂方法模板）
     *
     * @param factory 创建函数
     * @param <T>     值对象类型
     * @param <R>     输入参数类型
     * @return 值对象实例
     */
    protected static <T extends ValueObject<?>, R> T create(R input, java.util.function.Function<R, T> factory) {
        return factory.apply(input);
    }

    /**
     * 验证并创建值对象
     *
     * @param input        输入参数
     * @param validator    验证函数
     * @param factory      创建函数
     * @param errorMessage 错误消息
     * @param <T>          值对象类型
     * @param <R>          输入参数类型
     * @return 值对象实例
     */
    protected static <T extends ValueObject<?>, R> T validateAndCreate(
            R input,
            java.util.function.Predicate<R> validator,
            java.util.function.Function<R, T> factory,
            String errorMessage) {
        if (input == null || !validator.test(input)) {
            throw new DomainException(errorMessage);
        }
        return factory.apply(input);
    }
}
