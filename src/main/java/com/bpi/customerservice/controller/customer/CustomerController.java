package com.bpi.customerservice.controller.customer;

import com.bpi.customerservice.model.api.CustomerRequest;
import com.bpi.customerservice.model.api.CustomerResponse;
import com.bpi.customerservice.service.biz.CustomerService;
import org.springframework.http.HttpStatus;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
//@EnableAccessPolicies
@RequiredArgsConstructor
public class CustomerController implements CustomerControllerApi {

    private final CustomerService customerService;

    @Override
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer (
            @Parameter(description = "The apiKey used to authenticate access", required = true) @RequestHeader("apiKey") String apiKey,
            @Parameter(description = "The apiSecret used to authenticate access", required = true) @RequestHeader("apiSecret") String apiSecret,
            @Parameter(description = "Request Unique ID", required = true) @RequestHeader("requestUID") String requestUID,
            @Parameter(description = "Resource Owner ID - Online or Mobile", required = true) @RequestHeader("resourceOwnerID") String resourceOwnerID,
            @Parameter(description = "Customer details payload", required = true) @Valid @RequestBody CustomerRequest customerRequest) {

        return new ResponseEntity<>(customerService.createCustomer(customerRequest, requestUID, resourceOwnerID), HttpStatus.OK);    }

    @Override
    @GetMapping("/{customerNumber}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @Parameter(description = "The apiKey used to authenticate access", required = true) @RequestHeader("apiKey") String apiKey,
            @Parameter(description = "The apiSecret used to authenticate access", required = true) @RequestHeader("apiSecret") String apiSecret,
            @Parameter(description = "Request Unique ID", required = true) @RequestHeader("requestUID") String requestUID,
            @Parameter(description = "Resource Owner ID - Online or Mobile", required = true) @RequestHeader("resourceOwnerID") String resourceOwnerID,
            @Parameter(description = "Unique Customer Number", required = true) @PathVariable String customerNumber) {

        return new ResponseEntity<>(customerService.getCustomer(customerNumber, requestUID, resourceOwnerID), HttpStatus.OK);    }

    @Override
    @PutMapping("/{customerNumber}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @Parameter(description = "The apiKey used to authenticate access", required = true) @RequestHeader("apiKey") String apiKey,
            @Parameter(description = "The apiSecret used to authenticate access", required = true) @RequestHeader("apiSecret") String apiSecret,
            @Parameter(description = "Request Unique ID", required = true) @RequestHeader("requestUID") String requestUID,
            @Parameter(description = "Resource Owner ID - Online or Mobile", required = true) @RequestHeader("resourceOwnerID") String resourceOwnerID,
            @Parameter(description = "Unique Customer Number", required = true) @PathVariable String customerNumber,
            @Parameter(description = "Updated customer details", required = true) @Valid @RequestBody CustomerRequest customerRequest) {

        return new ResponseEntity<>(customerService.updateCustomer(customerNumber, customerRequest, requestUID, resourceOwnerID), HttpStatus.OK);    }

    @Override
    @DeleteMapping("/{customerNumber}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "The apiKey used to authenticate access", required = true) @RequestHeader("apiKey") String apiKey,
            @Parameter(description = "The apiSecret used to authenticate access", required = true) @RequestHeader("apiSecret") String apiSecret,
            @Parameter(description = "Request Unique ID", required = true) @RequestHeader("requestUID") String requestUID,
            @Parameter(description = "Resource Owner ID - Online or Mobile", required = true) @RequestHeader("resourceOwnerID") String resourceOwnerID,
            @Parameter(description = "Unique Customer Number", required = true) @PathVariable String customerNumber) {

        customerService.deleteCustomer(customerNumber, requestUID, resourceOwnerID);
        return new ResponseEntity<>(HttpStatus.OK);

    }
}