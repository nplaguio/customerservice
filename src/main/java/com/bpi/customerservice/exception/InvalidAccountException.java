package com.bpi.customerservice.exception;

import com.bpi.framework.commons.errorcode.ErrorCode;

public class InvalidAccountException extends RuntimeException {

    private final transient ErrorCode errorCode;

    public InvalidAccountException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}