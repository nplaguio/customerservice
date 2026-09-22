package com.bpi.customerservice.service.biz;

import com.bpi.customerservice.model.api.InquireBranchResponse;

public interface BranchInquiryMasterService {

    InquireBranchResponse executeBranchInquiry (String customerNumber, String accountNumber);

}