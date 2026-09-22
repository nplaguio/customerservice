package com.bpi.customerservice.service.api.impl;

import com.bpi.customerservice.model.api.InquireCustomerResponse;
import com.bpi.customerservice.model.biz.AccountDto;
import com.bpi.customerservice.model.biz.InquireCustomerBizDto;
import com.bpi.customerservice.service.biz.InquireCustomerMasterService;
import com.bpi.customerservice.service.api.InquireCustomerMainService;
import com.bpi.framework.web.component.RestResponseContentWrapper;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InquireCustomerMainServiceImpl implements InquireCustomerMainService {

    private final InquireCustomerMasterService inquireCustomerMasterApi;

    @Override
    public RestResponseContentWrapper<HttpHeaders, InquireCustomerResponse> processInquiry (String customerNumber, AccountDto request) {

        //changed to BizDto from 2 fields here
        InquireCustomerBizDto bizDto = InquireCustomerBizDto.builder()
                .customerNumber(customerNumber)
                .accountNumber(request.getAccountNumber())
                .build();

        InquireCustomerResponse response = inquireCustomerMasterApi.executeCustomerInquiry(bizDto);

        RestResponseContentWrapper<HttpHeaders, InquireCustomerResponse> wrapper = new RestResponseContentWrapper<>();
        wrapper.setBody(response);

        return wrapper;
    }
}