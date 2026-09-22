package com.bpi.customerservice.controller.branchinquiry;

import com.bpi.customerservice.model.api.InquireBranchResponse;
import com.bpi.customerservice.model.api.InquireCustomerResponse;
import com.bpi.customerservice.model.biz.AccountDto;
import com.bpi.framework.web.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface BranchInquiryControllerApi {

    @Operation(description = "Inquire Branch Details and Validate Account via SOAP", tags = { "Branch Inquiry" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = InquireCustomerResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content}),
            @ApiResponse(responseCode = "403", description = "Forbidden Access", content = {@Content}),
            @ApiResponse(responseCode = "404", description = "Resource Not Found", content = {@Content}),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred", content = {@Content}) })

    ResponseEntity<Response<InquireBranchResponse>> getBranchInquiry(
            @Parameter(description = "The apiKey used to authenticate access", required = true) String apiKey,
            @Parameter(description = "The apiSecret used to authenticate access", required = true) String apiSecret,
            @Parameter(description = "Request Unique ID", required = true) String requestUID,
            @Parameter(description = "Resource Owner ID - Online or Mobile", required = true) String resourceOwnerId,
            @Parameter(description = "Customer Number identifier", required = true, example = "12345") @PathVariable("customerNumber") String customerNumber,
            @Valid @RequestBody AccountDto request);
}