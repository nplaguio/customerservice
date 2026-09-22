package com.bpi.customerservice.repository.impl;

import com.bpi.customerservice.entity.AuditLog;
import com.bpi.customerservice.entity.Customer;
import com.bpi.customerservice.errorcode.customer.CustomerServiceErrorCode;
import com.bpi.customerservice.exception.CustomerNotFoundException;
import com.bpi.customerservice.repository.AuditLogRepository;
import com.bpi.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Repository
@RequiredArgsConstructor
public class CustomerDataManager {

    private final CustomerRepository customerRepository;
    private final AuditLogRepository auditLogRepository;

    public Customer saveAndLog(Customer customer, String requestUID, String resourceOwnerID, String action) {
        Customer savedCustomer = customerRepository.save(customer);
        logAction(requestUID, resourceOwnerID, action, savedCustomer.getCustomerNumber(), savedCustomer);
        return savedCustomer;
    }

    public Customer findByIdAndLog(String customerNumber, String requestUID, String resourceOwnerID) {
        Customer customer = customerRepository.findById(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException(CustomerServiceErrorCode.CUSTOMER_NOT_FOUND));

        logAction(requestUID, resourceOwnerID, "GET", customerNumber, customer);
        return customer;
    }

    public boolean existsById(String customerNumber) {
        return customerRepository.existsById(customerNumber);
    }

    public void deleteAndLog(Customer customer, String requestUID, String resourceOwnerID) {
        customerRepository.delete(customer);
        logAction(requestUID, resourceOwnerID, "DELETE", customer.getCustomerNumber(), customer);
    }

    private void logAction(String requestUID, String resourceOwnerID, String action, String customerNumber, Object details) {
        AuditLog log = new AuditLog(requestUID, resourceOwnerID, action, customerNumber, details,
                LocalDateTime.now(ZoneId.systemDefault()));
        auditLogRepository.save(log);
    }
}