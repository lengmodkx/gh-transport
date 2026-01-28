package com.ghtransport.common.exception;

import lombok.Getter;

/**
 * 领域异常
 */
@Getter
public class DomainException extends RuntimeException {

    private final String errorCode;

    public DomainException(String message) {
        super(message);
        this.errorCode = "DOMAIN_ERROR";
    }

    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
