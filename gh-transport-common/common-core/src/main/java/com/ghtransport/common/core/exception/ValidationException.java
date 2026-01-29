package com.ghtransport.common.core.exception;

import com.ghtransport.common.core.result.ResultCode;
import lombok.Getter;

import java.util.List;

/**
 * 参数校验异常
 */
@Getter
public class ValidationException extends BaseException {

    private static final long serialVersionUID = 1L;

    /**
     * 校验错误列表
     */
    private final List<String> errors;

    public ValidationException(String message) {
        super(ResultCode.PARAM_VALID_ERROR.getCode(), message);
        this.errors = List.of(message);
    }

    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
        this.errors = List.of(message);
    }

    public ValidationException(List<String> errors) {
        super(ResultCode.PARAM_VALID_ERROR.getCode(), String.join(", ", errors));
        this.errors = errors;
    }

    public ValidationException(String field, String message, String code) {
        super(code, message);
        this.errors = List.of(field + ": " + message);
    }
}
