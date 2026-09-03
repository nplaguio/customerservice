package com.bpi.customerservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name="customers")
@Data //generate getters, setters, and toString
@Builder
@NoArgsConstructor //creates empty constructor
@AllArgsConstructor //creates constructor with all fields
public class Customer {

    @Id
    @NotBlank(message = "Customer number is required")
    private String customerNumber;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "First Name is required")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    private String lastName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Birth Date is required")
    private LocalDate birthDate;

}