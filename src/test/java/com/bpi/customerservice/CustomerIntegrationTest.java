package com.bpi.customerservice;

import com.bpi.customerservice.entity.Customer;
import com.bpi.customerservice.repository.AuditLogRepository;
import com.bpi.customerservice.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        auditLogRepository.deleteAll();
    }

    @Test
    void testCreateCustomer_Success() throws Exception {
        Customer customer = Customer.builder()
                .customerNumber("123")
                .accountNumber("ACC001")
                .firstName("John")
                .lastName("Doe")
                .address("Manila")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        mockMvc.perform(post("/api/customers")
                        .header("requestUID", "req-001")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerNumber").value("123"));

        assertEquals(1, customerRepository.count());
        assertEquals(1, auditLogRepository.count());
    }

    @Test
    void testGetCustomer_Success() throws Exception {
        Customer customer = Customer.builder()
                .customerNumber("123")
                .accountNumber("ACC001")
                .firstName("John")
                .lastName("Doe")
                .address("Manila")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        customerRepository.save(customer);

        mockMvc.perform(get("/api/customers/123")
                        .header("requestUID", "req-002")
                        .header("resourceOwnerID", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void testGetCustomer_NotFound_Returns400() throws Exception {
        mockMvc.perform(get("/api/customers/999")
                        .header("requestUID", "req-003")
                        .header("resourceOwnerID", "user-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testUpdateCustomer_Success() throws Exception {
        Customer original = Customer.builder()
                .customerNumber("123")
                .accountNumber("ACC001")
                .firstName("John")
                .lastName("Doe")
                .address("Manila")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        customerRepository.save(original);

        Customer updated = Customer.builder()
                .customerNumber("123")
                .accountNumber("ACC001")
                .firstName("Jane")
                .lastName("Smith")
                .address("Cebu")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        mockMvc.perform(put("/api/customers/123")
                        .header("requestUID", "req-004")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void testDeleteCustomer_Success() throws Exception {
        Customer customer = Customer.builder()
                .customerNumber("123")
                .accountNumber("ACC001")
                .firstName("John")
                .lastName("Doe")
                .address("Manila")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
        customerRepository.save(customer);

        mockMvc.perform(delete("/api/customers/123")
                        .header("requestUID", "req-005")
                        .header("resourceOwnerID", "user-001"))
                .andExpect(status().isNoContent());

        assertEquals(0, customerRepository.count());
    }

    @Test
    void testValidationError_Returns400() throws Exception {
        // Missing firstName to trigger validation error
        Customer invalidCustomer = Customer.builder()
                .customerNumber("124")
                .accountNumber("ACC002")
                .firstName("")
                .lastName("Doe")
                .address("Manila")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        mockMvc.perform(post("/api/customers")
                        .header("requestUID", "req-006")
                        .header("resourceOwnerID", "user-002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCustomer)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testInvalidApi_Returns404() throws Exception {
        mockMvc.perform(get("/api/fake-endpoint"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateCustomer_NotFound_Returns400() throws Exception {
        Customer updatedCustomer = Customer.builder()
                .customerNumber("999")
                .accountNumber("ACC999")
                .firstName("Ghost")
                .lastName("User")
                .address("Nowhere")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        mockMvc.perform(put("/api/customers/999")
                        .header("requestUID", "req-007")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCustomer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testAuditLogEntity_BoostCoverage() {
        com.bpi.customerservice.entity.AuditLog log = new com.bpi.customerservice.entity.AuditLog();
        log.setId("log-1");
        log.setRequestUID("req-1");
        log.setResourceOwnerID("owner-1");
        log.setAction("TEST");
        log.setCustomerNumber("123");
        log.setCustomerDetails("Details");
        log.setTimestamp(java.time.LocalDateTime.now());

        assertEquals("log-1", log.getId());
        assertEquals("req-1", log.getRequestUID());
        assertEquals("owner-1", log.getResourceOwnerID());
        assertEquals("TEST", log.getAction());
        assertEquals("123", log.getCustomerNumber());
        assertEquals("Details", log.getCustomerDetails());
        org.junit.jupiter.api.Assertions.assertNotNull(log.getTimestamp());
    }
}