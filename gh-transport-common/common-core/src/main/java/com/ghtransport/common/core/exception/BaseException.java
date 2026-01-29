package com.ghtransport.common.core.exception;

import com.ghtransport.common.core.result.ResultCode;
import lombok.Getter;

import java.util.Map;

/**
 * 基础业务异常
 */
@Getter
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误消息
     */
    private final String errorMessage;

    /**
     * 错误级别
     */
    private final ErrorLevel level;

    /**
     * 上下文信息
     */
    private final Map<String, Object> context;

    public BaseException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.level = ErrorLevel.ERROR;
        this.context = null;
    }

    public BaseException(String errorCode, String errorMessage, ErrorLevel level) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.level = level;
        this.context = null;
    }

    public BaseException(String errorCode, String errorMessage, Map<String, Object> context) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.level = ErrorLevel.ERROR;
        this.context = context;
    }

    public BaseException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.errorCode = resultCode.getCode();
        this.errorMessage = resultCode.getMessage();
        this.level = ErrorLevel.ERROR;
        this.context = null;
    }

    public BaseException(ResultCode resultCode, ErrorLevel level) {
        super(resultCode.getMessage());
        this.errorCode = resultCode.getCode();
        this.errorMessage = resultCode.getMessage();
        this.level = level;
        this.context = null;
    }

    public BaseException(ResultCode resultCode, Map<String, Object> context) {
        super(resultCode.getMessage());
        this.errorCode = resultCode.getCode();
        this.errorMessage = resultCode.getMessage();
        this.level = ErrorLevel.ERROR;
        this.context = context;
    }

    public BaseException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.level = ErrorLevel.ERROR;
        this.context = null;
    }

    /**
     * 错误级别枚举
     */
    public enum ErrorLevel {
        /**
         * 警告 - 需要关注
         */
        WARN,
        /**
         * 错误 - 系统错误
         */
        ERROR,
        /**
         * 严重 - 致命错误
         */
        FATAL
    }
}
