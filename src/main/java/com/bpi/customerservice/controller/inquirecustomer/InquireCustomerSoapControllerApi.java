package com.bpi.customerservice.controller.inquirecustomer;

import com.bpi.customerservice.model.api.InquireCustomerResponse;
import com.bpi.customerservice.model.biz.AccountDto;
import com.bpi.framework.web.model.Response;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public interface InquireCustomerSoapControllerApi {

    ResponseEntity<Response<InquireCustomerResponse>> getCustomerInquiry(
            @RequestHeader("apiKey") String apiKey,
            @RequestHeader("apiSecret") String apiSecret,
            @RequestHeader("requestUID") String requestUID,
            @RequestHeader("resourceOwnerID") String resourceOwnerId,
            @PathVariable String customerNumber,
            @Valid @RequestBody AccountDto request
    );
}