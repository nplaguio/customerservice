package com.bpi.customerservice.model.api;

import com.bpi.customerservice.model.biz.AccountDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CustomerResponse {

    private String customerNumber;

    private String firstName;

    private String lastName;

    private String address;

    private LocalDate birthDate;

    private List<AccountDto> accounts;

}