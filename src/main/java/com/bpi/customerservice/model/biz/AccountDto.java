package com.bpi.customerservice.model.biz;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AccountDto {

    @NotBlank(message = "Account number cannot be blank or missing")
    private String accountNumber;

}