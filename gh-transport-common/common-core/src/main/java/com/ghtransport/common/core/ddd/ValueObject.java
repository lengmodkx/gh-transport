package com.ghtransport.common.core.ddd;

import java.lang.annotation.*;

/**
 * 值对象注解
 *
 * 值对象的特点：
 * 1. 通过属性值来识别
 * 2. 不可变
 * 3. 无身份标识
 * 4. 可以组合其他值对象
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValueObject {
}
