package com.bpi.customerservice.service.biz;

import com.bpi.customerservice.model.api.CustomerRequest;
import com.bpi.customerservice.model.api.CustomerResponse;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request, String requestUID, String resourceOwnerID);

    CustomerResponse getCustomer(String customerNumber, String requestUID, String resourceOwnerID);

    CustomerResponse updateCustomer(String customerNumber, CustomerRequest request, String requestUID, String resourceOwnerID);

    Void deleteCustomer(String customerNumber, String requestUID, String resourceOwnerID);
}