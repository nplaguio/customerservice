package com.bpi.customerservice.controller;

import com.bpi.customerservice.entity.Customer;
import com.bpi.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(
            @Valid @RequestBody Customer customer,
            @RequestHeader("requestUID") String requestUID,
            @RequestHeader("resourceOwnerID") String resourceOwnerID) {

        Customer createdCustomer = customerService.createCustomer(customer, requestUID, resourceOwnerID);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @GetMapping("/{customerNumber}")
    public ResponseEntity<Customer> getCustomer(
            @PathVariable String customerNumber,
            @RequestHeader("requestUID") String requestUID,
            @RequestHeader("resourceOwnerID") String resourceOwnerID) {

        Customer customer = customerService.getCustomer(customerNumber, requestUID, resourceOwnerID);
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{customerNumber}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable String customerNumber,
            @Valid @RequestBody Customer customer,
            @RequestHeader("requestUID") String requestUID,
            @RequestHeader("resourceOwnerID") String resourceOwnerID) {

        Customer updatedCustomer = customerService.updateCustomer(customerNumber, customer, requestUID, resourceOwnerID);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{customerNumber}")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable String customerNumber,
            @RequestHeader("requestUID") String requestUID,
            @RequestHeader("resourceOwnerID") String resourceOwnerID) {

        customerService.deleteCustomer(customerNumber, requestUID, resourceOwnerID);
        return ResponseEntity.noContent().build();
    }
}