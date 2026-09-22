package com.bpi.customerservice.model.biz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquireCustomerBizDto {

    private String customerNumber;

    private String accountNumber;

}