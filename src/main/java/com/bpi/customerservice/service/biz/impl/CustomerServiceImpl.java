package com.bpi.customerservice.service.biz.impl;

import com.bpi.customerservice.entity.Customer;
import com.bpi.customerservice.model.api.CustomerRequest;
import com.bpi.customerservice.model.api.CustomerResponse;
import com.bpi.customerservice.errorcode.customer.CustomerServiceErrorCode;
import com.bpi.customerservice.exception.CustomerNotFoundException;
import com.bpi.customerservice.repository.impl.CustomerDataManager;
import com.bpi.customerservice.service.biz.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDataManager customerDataManager;

    @Override
    public CustomerResponse createCustomer (CustomerRequest request, String requestUID, String resourceOwnerID) {
        Customer customerEntity = Customer.builder()
                .customerNumber(request.getCustomerNumber())
                .accountNumber(request.getAccountNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .address(request.getAddress())
                .birthDate(request.getBirthDate())
                .build();

        Customer savedCustomer = customerDataManager.saveAndLog(customerEntity, requestUID, resourceOwnerID, "POST");

        //straight through/no wrapper
        return mapToResponseDto(savedCustomer);
    }

    @Override
    public CustomerResponse getCustomer (String customerNumber, String requestUID, String resourceOwnerID) {
        Customer customer = customerDataManager.findByIdAndLog(customerNumber, requestUID, resourceOwnerID);

        //straight through/no wrapper
        return mapToResponseDto(customer);
    }

    @Override
    public CustomerResponse updateCustomer (String customerNumber, CustomerRequest request, String requestUID, String resourceOwnerID) {
        if (!customerDataManager.existsById(customerNumber)) {
            throw new CustomerNotFoundException(CustomerServiceErrorCode.CUSTOMER_NOT_FOUND);
        }

        Customer updatedCustomerEntity = Customer.builder()
                .customerNumber(customerNumber)
                .accountNumber(request.getAccountNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .address(request.getAddress())
                .birthDate(request.getBirthDate())
                .build();

        Customer savedCustomer = customerDataManager.saveAndLog(updatedCustomerEntity, requestUID, resourceOwnerID, "PUT");

        //straight through/no wrapper
        return mapToResponseDto(savedCustomer);
    }

    @Override
    public Void deleteCustomer (String customerNumber, String requestUID, String resourceOwnerID) {
        Customer customer = customerDataManager.findByIdAndLog(customerNumber, requestUID, resourceOwnerID);
        customerDataManager.deleteAndLog(customer, requestUID, resourceOwnerID);

        //straight through/no wrapper
        return null;
    }

    private CustomerResponse mapToResponseDto(Customer entity) {
        return CustomerResponse.builder()
                .customerNumber(entity.getCustomerNumber())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .address(entity.getAddress())
                .birthDate(entity.getBirthDate())
                .build();
    }
}