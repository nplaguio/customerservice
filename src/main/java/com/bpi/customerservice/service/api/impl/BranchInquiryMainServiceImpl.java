package com.bpi.customerservice.service.api.impl;

import com.bpi.customerservice.errorcode.customer.CustomerServiceErrorCode;
import com.bpi.customerservice.exception.CustomerNotFoundException;
import com.bpi.customerservice.model.api.InquireBranchResponse;
import com.bpi.customerservice.model.biz.AccountDto;
import com.bpi.customerservice.repository.CustomerRepository;
import com.bpi.customerservice.service.api.BranchInquiryMainService;
import com.bpi.customerservice.service.biz.BranchInquiryMasterService;
import com.bpi.framework.web.component.RestResponseContentWrapper;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BranchInquiryMainServiceImpl implements BranchInquiryMainService {

    private final BranchInquiryMasterService branchInquiryMasterApi;
    private final CustomerRepository customerRepository;

    @Override
    public RestResponseContentWrapper<HttpHeaders, InquireBranchResponse> processInquiry (String customerNumber, AccountDto request) {

        //done putting it here from branchinquirymasterservice
        if (!customerRepository.existsById(customerNumber)) {
            throw new CustomerNotFoundException(CustomerServiceErrorCode.CUSTOMER_NOT_FOUND);
        }

        InquireBranchResponse response = branchInquiryMasterApi.executeBranchInquiry(customerNumber, request.getAccountNumber());

        RestResponseContentWrapper<HttpHeaders, InquireBranchResponse> wrapper = new RestResponseContentWrapper<>();
        wrapper.setBody(response);

        return wrapper;
    }
}