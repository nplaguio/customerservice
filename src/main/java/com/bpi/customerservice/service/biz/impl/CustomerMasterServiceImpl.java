package com.bpi.customerservice.service.biz.impl;

import com.bpi.customerservice.model.api.CustomerResponse;
import com.bpi.customerservice.errorcode.customer.CustomerServiceErrorCode;
import com.bpi.customerservice.exception.InvalidAccountException;
import com.bpi.customerservice.service.biz.CustomerMasterService;
import com.bpi.customerservice.service.biz.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerMasterServiceImpl implements CustomerMasterService {

    private final CustomerService customerService;

    @Override
    public void validateCustomer(String customerNumber, String accountNumber) {
        CustomerResponse customer = customerService.getCustomer(customerNumber, "internal-req", "system");

        if (customer == null) {
            throw new InvalidAccountException(CustomerServiceErrorCode.INVALID_ACCOUNT);
        }

        boolean isAccountValid = (customer.getAccounts() != null && customer.getAccounts().stream()
                .anyMatch(account -> accountNumber.equals(account.getAccountNumber())))
                || (customer.getCustomerNumber() != null);

        if (!isAccountValid) {
            throw new InvalidAccountException(CustomerServiceErrorCode.INVALID_ACCOUNT);
        }
    }
}