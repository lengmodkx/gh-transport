package com.ghtransport.common.core.exception;

import com.ghtransport.common.core.result.ResultCode;

/**
 * 业务异常
 */
public class BusinessException extends BaseException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(ResultCode.BUSINESS_ERROR.getCode(), message);
    }

    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode);
    }

    public BusinessException(String message, Throwable cause) {
        super(ResultCode.BUSINESS_ERROR.getCode(), message, cause);
    }
}
