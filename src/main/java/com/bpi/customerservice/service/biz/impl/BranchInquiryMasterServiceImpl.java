package com.bpi.customerservice.service.biz.impl;

import com.bpi.customerservice.model.api.InquireBranchResponse;
import com.bpi.customerservice.model.ws.InquireBranchRequest;
import com.bpi.customerservice.errorcode.branch.InquireBranchErrorCode;
import com.bpi.customerservice.errorcode.customer.CustomerServiceErrorCode;
import com.bpi.customerservice.exception.BranchServiceException;
import com.bpi.customerservice.exception.InvalidAccountException;
import com.bpi.customerservice.model.ws.InquireBranchWsResponse;
import com.bpi.customerservice.service.biz.BranchInquiryMasterService;
import com.bpi.customerservice.service.ws.InquireBranchWsService;
import org.springframework.core.convert.converter.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchInquiryMasterServiceImpl implements BranchInquiryMasterService {

    private final InquireBranchWsService inquireBranchApi;

    //2 new converters: branchinquiry request and response cconverter
    private final Converter<String, InquireBranchRequest> branchRequestConverter;
    private final Converter<InquireBranchWsResponse.InquireBranchOutput, InquireBranchResponse> branchResponseConverter;

    @Override
    public InquireBranchResponse executeBranchInquiry (String customerNumber, String accountNumber) {

        InquireBranchRequest downstreamRequest = branchRequestConverter.convert(accountNumber);

        InquireBranchWsResponse branchResponse;

        try {
            branchResponse = inquireBranchApi.callBranchService(downstreamRequest);
        } catch (Exception e) {
            log.error("Error occurred while calling Branch Service", e);
            throw new BranchServiceException(InquireBranchErrorCode.SERVICE_ERROR);
        }

        if (branchResponse == null ||
                branchResponse.getInquireBranchOperationResponse() == null ||
                branchResponse.getInquireBranchOperationResponse().getInquireBranchOutput() == null ||
                !"0".equals(branchResponse.getInquireBranchOperationResponse().getInquireBranchOutput().getResponseCode())) {

            throw new InvalidAccountException(CustomerServiceErrorCode.CUSTOMER_NOT_FOUND);
        }

        return branchResponseConverter.convert(branchResponse.getInquireBranchOperationResponse().getInquireBranchOutput());
    }
}