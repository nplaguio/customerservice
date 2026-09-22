package com.bpi.customerservice.controller.branchinquiry;

import com.bpi.customerservice.model.api.InquireBranchResponse;
import com.bpi.customerservice.model.biz.AccountDto;
import com.bpi.customerservice.service.api.BranchInquiryMainService;
import com.bpi.framework.web.component.ResponseConverter;
import com.bpi.framework.web.model.Response;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
//@EnableAccessPolicies
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class BranchInquiryController implements BranchInquiryControllerApi {

    private final BranchInquiryMainService branchInquiryService;

    @Override
    @PostMapping("/{customerNumber}/inquire-branch")
    public ResponseEntity<Response<InquireBranchResponse>> getBranchInquiry (
            @Parameter(description = "The apiKey used to authenticate access", required = true) @RequestHeader("apiKey") String apiKey,
            @Parameter(description = "The apiSecret used to authenticate access", required = true) @RequestHeader("apiSecret") String apiSecret,
            @Parameter(description = "Request Unique ID", required = true) @RequestHeader("requestUID") String requestUID,
            @Parameter(description = "Resource Owner ID", required = true) @RequestHeader("resourceOwnerID") String resourceOwnerId,
            @Parameter(description = "Customer Number identifier", required = true, example = "12345") @PathVariable("customerNumber") String customerNumber,
            @Valid @RequestBody AccountDto request) {

        return ResponseConverter.convert(branchInquiryService.processInquiry(customerNumber, request));
    }
}