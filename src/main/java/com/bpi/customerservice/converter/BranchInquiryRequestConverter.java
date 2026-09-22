package com.bpi.customerservice.converter;

import com.bpi.customerservice.model.ws.InquireBranchRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BranchInquiryRequestConverter implements Converter<String, InquireBranchRequest> {

    @Override
    public InquireBranchRequest convert(String accountNumber) {
        InquireBranchRequest.InquireBranchInput input = new InquireBranchRequest.InquireBranchInput();
        input.setAccountNumber(accountNumber);

        InquireBranchRequest.InquireBranchOperationRequest operationRequest = new InquireBranchRequest.InquireBranchOperationRequest();
        operationRequest.setInquireBranchInput(input);

        InquireBranchRequest downstreamRequest = new InquireBranchRequest();
        downstreamRequest.setInquireBranchOperationRequest(operationRequest);

        return downstreamRequest;
    }
}