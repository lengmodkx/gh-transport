package com.ghtransport.order.interfaces.exception;

import com.ghtransport.common.exception.DomainException;
import com.ghtransport.order.interfaces.controller.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class OrderExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public Result<Void> handleDomainException(DomainException e) {
        log.error("Order domain exception: {}", e.getMessage());
        return Result.error(e.getErrorCode(), e.getMessage());
    }
}
