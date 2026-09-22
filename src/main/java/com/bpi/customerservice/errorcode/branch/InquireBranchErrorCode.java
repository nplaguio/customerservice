package com.bpi.customerservice.errorcode.branch;

import com.bpi.framework.commons.errorcode.ErrorCode;

public class InquireBranchErrorCode extends ErrorCode {

    protected InquireBranchErrorCode(String code, String message) {
        super(code, message);
    }

    public static final ErrorCode SERVICE_ERROR = new InquireBranchErrorCode("SKSSE999", "There is a problem with Inquire Branch Microservice");

}