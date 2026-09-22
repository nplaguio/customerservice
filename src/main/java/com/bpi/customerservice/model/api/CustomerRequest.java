package com.bpi.customerservice.model.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerRequest {

    @Size(max = 20, message = "Account Number must be at 20 characters")
    private String accountNumber;

    private String customerNumber;

    @NotBlank(message = "First Name is required")
    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address name must be at most 255 characters")
    private String address;

    @NotNull(message = "Birth Date is required!")
    @Past(message = "Birt date must be in the past")
    private LocalDate birthDate;
}