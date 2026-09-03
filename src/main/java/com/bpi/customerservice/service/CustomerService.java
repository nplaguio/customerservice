package com.bpi.customerservice.service;

import com.bpi.customerservice.entity.AuditLog;
import com.bpi.customerservice.entity.Customer;
import com.bpi.customerservice.exception.CustomerNotFoundException;
import com.bpi.customerservice.repository.AuditLogRepository;
import com.bpi.customerservice.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditLogRepository auditLogRepository;

    public CustomerService(CustomerRepository customerRepository, AuditLogRepository auditLogRepository) {
        this.customerRepository = customerRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public Customer createCustomer(Customer customer, String requestUID, String resourceOwnerID) {
        Customer savedCustomer = customerRepository.save(customer);
        logAction(requestUID, resourceOwnerID, "POST", savedCustomer.getCustomerNumber(), savedCustomer);
        return savedCustomer;
    }

    public Customer getCustomer(String customerNumber, String requestUID, String resourceOwnerID) {
        Customer customer = customerRepository.findById(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerNumber));

        logAction(requestUID, resourceOwnerID, "GET", customerNumber, customer);
        return customer;
    }

    public Customer updateCustomer(String customerNumber, Customer updatedCustomer, String requestUID, String resourceOwnerID) {
        if (!customerRepository.existsById(customerNumber)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + customerNumber);
        }

        updatedCustomer.setCustomerNumber(customerNumber);
        Customer savedCustomer = customerRepository.save(updatedCustomer);

        logAction(requestUID, resourceOwnerID, "PUT", customerNumber, savedCustomer);
        return savedCustomer;
    }

    public void deleteCustomer(String customerNumber, String requestUID, String resourceOwnerID) {
        Customer customer = customerRepository.findById(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerNumber));

        customerRepository.delete(customer);
        logAction(requestUID, resourceOwnerID, "DELETE", customerNumber, customer);
    }

    private void logAction(String requestUID, String resourceOwnerID, String action, String customerNumber, Object details) {
        AuditLog log = new AuditLog(requestUID, resourceOwnerID, action, customerNumber, details, LocalDateTime.now());
        auditLogRepository.save(log);
    }
}