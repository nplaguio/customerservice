package com.bpi.customerservice.service.api;

import com.bpi.customerservice.model.api.InquireCustomerResponse;
import com.bpi.customerservice.model.biz.AccountDto;
import com.bpi.framework.web.component.RestResponseContentWrapper;
import org.springframework.http.HttpHeaders;

public interface InquireCustomerMainService {

    RestResponseContentWrapper<HttpHeaders, InquireCustomerResponse> processInquiry (String customerNumber, AccountDto request);

}