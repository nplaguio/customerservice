package com.bpi.customerservice.errorcode.customer;

import com.bpi.framework.commons.errorcode.ErrorCode;

public class CustomerServiceErrorCode extends ErrorCode {

    public static final ErrorCode INTERNAL_ERROR = new CustomerServiceErrorCode("CUSIE999", "Internal Error");
    public static final ErrorCode SERVICE_ERROR = new CustomerServiceErrorCode("CUSSE999", "External Service Error");
    public static final ErrorCode CUSTOMER_NOT_FOUND = new CustomerServiceErrorCode("CUSBE001", "Customer not found");
    public static final ErrorCode INVALID_ACCOUNT = new CustomerServiceErrorCode("CUSVE001", "Invalid Account Number");

    public CustomerServiceErrorCode(String code, String message) {
        super(code, message);
    }
}
