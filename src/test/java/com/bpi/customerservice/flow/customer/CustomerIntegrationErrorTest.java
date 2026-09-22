package com.bpi.customerservice.flow.customer;

import com.bpi.customerservice.CustomerserviceApplicationTests;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CustomerIntegrationErrorTest extends CustomerserviceApplicationTests {


    @Test
    void testInquireBranch_CustomerNotFound_Returns400() throws Exception {
        String requestBody = """
                {
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/customers/NONEXISTENT_ID/inquire-branch")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-001")
                        .header("resourceOwnerID", "user-000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCustomer_NotFound_Returns400() throws Exception {
        mockMvc.perform(get("/api/customers/999")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-003")
                        .header("resourceOwnerID", "user-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CUSBE001"))
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    void testValidationError_Returns400() throws Exception {
        String requestBody = """
                {
                    "customerNumber": "124",
                    "accountNumber": "ACC002",
                    "firstName": "",
                    "lastName": "Doe",
                    "address": "Manila",
                    "birthDate": "1990-01-01"
                }
                """;

        mockMvc.perform(post("/api/customers")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-006")
                        .header("resourceOwnerID", "user-002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                // Assert the actual field validation error returned by the app
                .andExpect(jsonPath("$.firstName").value("First Name is required"));
    }

    @Test
    void testInvalidApi_Returns404() throws Exception {
        mockMvc.perform(get("/api/fake-endpoint"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateCustomer_NotFound_Returns400() throws Exception {
        String requestBody = """
                {
                    "customerNumber": "999",
                    "accountNumber": "1234567899",
                    "firstName": "Ghost",
                    "lastName": "User",
                    "address": "Nowhere",
                    "birthDate": "1990-01-01"
                }
                """;

        mockMvc.perform(put("/api/customers/999")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-007")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CUSBE001"))
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    void testValidateCustomer_CustomerNotFound_Returns400() throws Exception {
        String requestBody = """
                {
                    "accountNumber": "1234567890"
                }
                """;

        mockMvc.perform(post("/api/customers/NONEXISTENT_CUST/inquire-branch")
                        .header("apiKey", "test-api-key")
                        .header("apiSecret", "test-api-secret")
                        .header("requestUID", "req-validate-err")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }



}