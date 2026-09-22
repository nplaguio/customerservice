package com.bpi.customerservice.controller.inquirecustomer;

import com.bpi.customerservice.model.api.InquireCustomerResponse;
import com.bpi.customerservice.model.biz.AccountDto;
import com.bpi.customerservice.service.api.InquireCustomerMainService;
import com.bpi.framework.web.component.ResponseConverter;
import com.bpi.framework.web.model.Response;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class InquireCustomerSoapController implements InquireCustomerSoapControllerApi {

    private final InquireCustomerMainService inquireCustomerMain;

    @Override
    @PostMapping("/{customerNumber}/inquire-customer")
    public ResponseEntity<Response<InquireCustomerResponse>> getCustomerInquiry (
            @Parameter(description = "The apiKey used to authenticate access", required = true) @RequestHeader("apiKey") String apiKey,
            @Parameter(description = "The apiSecret used to authenticate access", required = true) @RequestHeader("apiSecret") String apiSecret,
            @Parameter(description = "Request Unique ID", required = true) @RequestHeader("requestUID") String requestUID,
            @Parameter(description = "Resource Owner ID", required = true) @RequestHeader("resourceOwnerID") String resourceOwnerId,
            @PathVariable String customerNumber,
            @Valid @RequestBody AccountDto request) {

        return ResponseConverter.convert(inquireCustomerMain.processInquiry(customerNumber, request));
    }
}