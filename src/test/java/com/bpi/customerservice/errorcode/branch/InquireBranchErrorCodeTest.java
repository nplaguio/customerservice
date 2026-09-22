package com.bpi.customerservice.errorcode.branch;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class InquireBranchErrorCodeTest {

    @Test
    void testErrorCodeConstants() {
        assertNotNull(InquireBranchErrorCode.SERVICE_ERROR);
        assertEquals("SKSSE999", InquireBranchErrorCode.SERVICE_ERROR.getCode());
        assertEquals("There is a problem with Inquire Branch Microservice", InquireBranchErrorCode.SERVICE_ERROR.getMessage());
    }
}