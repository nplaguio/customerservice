package com.bpi.customerservice.exception;

import com.bpi.framework.commons.errorcode.ErrorCode;

public class BranchServiceException extends RuntimeException {

    private final transient ErrorCode errorCode;

    public BranchServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}