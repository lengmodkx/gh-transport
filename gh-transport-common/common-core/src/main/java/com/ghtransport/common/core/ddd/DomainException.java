package com.ghtransport.common.core.ddd;

import com.ghtransport.common.core.exception.BaseException;

/**
 * 领域异常
 *
 * 领域异常表示在业务规则校验或领域操作中出现的错误
 */
public class DomainException extends BaseException {

    private static final long serialVersionUID = 1L;

    public DomainException(String message) {
        super("DOMAIN_ERROR", message);
    }

    public DomainException(String errorCode, String message) {
        super(errorCode, message);
    }

    public DomainException(String errorCode, String message, BaseException.ErrorLevel level) {
        super(errorCode, message, level);
    }

    public DomainException(String message, Throwable cause) {
        super("DOMAIN_ERROR", message, cause);
    }

    /**
     * 创建领域异常
     */
    public static DomainException of(String message) {
        return new DomainException(message);
    }

    /**
     * 创建领域异常（带错误码）
     */
    public static DomainException of(String errorCode, String message) {
        return new DomainException(errorCode, message);
    }
}
