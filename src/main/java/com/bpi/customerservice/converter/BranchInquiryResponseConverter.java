package com.bpi.customerservice.converter;

import com.bpi.customerservice.model.api.InquireBranchResponse;
import com.bpi.customerservice.model.ws.InquireBranchWsResponse;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BranchInquiryResponseConverter implements Converter<InquireBranchWsResponse.InquireBranchOutput, InquireBranchResponse> {

    @Override
    public InquireBranchResponse convert(InquireBranchWsResponse.InquireBranchOutput source) {
        if (source == null) {
            return null;
        }

        InquireBranchResponse.InquireBranchOutput responseOutput = InquireBranchResponse.InquireBranchOutput.builder()
                .accountNumber(source.getAccountNumber())
                .control2(source.getControl2() != null ? source.getControl2() : "")
                .fileStat(source.getFileStat() != null ? source.getFileStat() : "")
                .accountControls(source.getAccountControls() != null ? source.getAccountControls() : "")
                .realBranch(source.getRealBranch())
                .branchName(source.getBranchName())
                .responseCode(source.getResponseCode())
                .responseDescription(source.getResponseDescription())
                .build();

        return InquireBranchResponse.builder()
                .inquireBranchOperationResponse(
                        InquireBranchResponse.InquireBranchOperationResponse.builder()
                                .inquireBranchOutput(responseOutput)
                                .build()
                )
                .build();
    }
}