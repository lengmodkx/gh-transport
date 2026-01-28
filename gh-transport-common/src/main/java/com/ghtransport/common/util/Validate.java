package com.ghtransport.common.util;

import com.ghtransport.common.exception.DomainException;

/**
 * 校验工具
 */
public class Validate {

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new DomainException(message);
        }
    }

    public static void notBlank(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new DomainException(message);
        }
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new DomainException(message);
        }
    }
}
