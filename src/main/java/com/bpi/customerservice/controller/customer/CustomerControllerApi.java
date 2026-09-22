package com.bpi.customerservice.controller.customer;

import com.bpi.customerservice.model.api.CustomerRequest;
import com.bpi.customerservice.model.api.CustomerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public interface CustomerControllerApi {

    @Operation(summary = "Create Customer", description = "Registers a new customer in the system.", tags = {"Customer Management"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = {@Content(schema = @Schema(implementation = CustomerResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Validation Error", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    ResponseEntity<CustomerResponse> createCustomer(
            @Parameter(description = "The apiKey used to authenticate access", required = true) @RequestHeader("apiKey") String apiKey,
            @Parameter(description = "The apiSecret used to authenticate access", required = true) @RequestHeader("apiSecret") String apiSecret,
            @Parameter(description = "Request Unique ID", required = true) @RequestHeader("requestUID") String requestUID,
            @Parameter(description = "Resource Owner ID - Online or Mobile", required = true) @RequestHeader("resourceOwnerID") String resourceOwnerID,
            @Parameter(description = "Customer details payload", required = true) @RequestBody CustomerRequest customerRequest
    );

    @Operation(summary = "Get Customer Details", description = "Retrieves an existing customer by their customer number.", tags = {"Customer Management"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = CustomerResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Customer Not Found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    ResponseEntity<CustomerResponse> getCustomer(
            @Parameter(description = "The apiKey used to authenticate access", required = true) @RequestHeader("apiKey") String apiKey,
            @Parameter(description = "The apiSecret used to authenticate access", required = true) @RequestHeader("apiSecret") String apiSecret,
            @Parameter(description = "Request Unique ID", required = true) @RequestHeader("requestUID") String requestUID,
            @Parameter(description = "Resource Owner ID - Online or Mobile", required = true) @RequestHeader("resourceOwnerID") String resourceOwnerID,
            @Parameter(description = "Unique Customer Number", required = true) @PathVariable String customerNumber
    );

    @Operation(summary = "Update Customer", description = "Updates an existing customer's information.", tags = {"Customer Management"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(schema = @Schema(implementation = CustomerResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer Not Found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    ResponseEntity<CustomerResponse> updateCustomer(
            @Parameter(description = "The apiKey used to authenticate access", required = true) @RequestHeader("apiKey") String apiKey,
            @Parameter(description = "The apiSecret used to authenticate access", required = true) @RequestHeader("apiSecret") String apiSecret,
            @Parameter(description = "Request Unique ID", required = true) @RequestHeader("requestUID") String requestUID,
            @Parameter(description = "Resource Owner ID - Online or Mobile", required = true) @RequestHeader("resourceOwnerID") String resourceOwnerID,
            @Parameter(description = "Unique Customer Number", required = true) @PathVariable String customerNumber,
            @Parameter(description = "Updated customer details", required = true) @RequestBody CustomerRequest customerRequest
    );

    @Operation(summary = "Delete Customer", description = "Removes a customer from the system.", tags = {"Customer Management"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content - Successfully Deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer Not Found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "The apiKey used to authenticate access", required = true) @RequestHeader("apiKey") String apiKey,
            @Parameter(description = "The apiSecret used to authenticate access", required = true) @RequestHeader("apiSecret") String apiSecret,
            @Parameter(description = "Request Unique ID", required = true) @RequestHeader("requestUID") String requestUID,
            @Parameter(description = "Resource Owner ID - Online or Mobile", required = true) @RequestHeader("resourceOwnerID") String resourceOwnerID,
            @Parameter(description = "Unique Customer Number", required = true) @PathVariable String customerNumber
    );
}