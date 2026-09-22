package com.bpi.customerservice.service.api;

import com.bpi.customerservice.model.api.InquireBranchResponse;
import com.bpi.customerservice.model.biz.AccountDto;
import com.bpi.framework.web.component.RestResponseContentWrapper;
import org.springframework.http.HttpHeaders;

public interface BranchInquiryMainService {

    RestResponseContentWrapper<HttpHeaders, InquireBranchResponse> processInquiry (String customerNumber, AccountDto request);

}