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
        // Clear databases before each test to ensure a clean slate
        customerRepository.deleteAll();
        auditLogRepository.deleteAll();
    }

    @Test
    void testCreateCustomer_Success() throws Exception {
        Customer customer = new Customer("123", "ACC001", "John", "Doe", "Manila", LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/customers")
                        .header("requestUID", "req-001")
                        .header("resourceOwnerID", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerNumber").value("123"));

        assertEquals(1, customerRepository.count());
        assertEquals(1, auditLogRepository.count()); // Verifies MongoDB Audit Log
    }

    @Test
    void testGetCustomer_Success() throws Exception {
        // Setup existing customer
        Customer customer = new Customer("123", "ACC001", "John", "Doe", "Manila", LocalDate.of(1990, 1, 1));
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
                .andExpect(status().isBadRequest()) // Task 1.3 Rule: 400 Bad Request
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testUpdateCustomer_Success() throws Exception {
        Customer original = new Customer("123", "ACC001", "John", "Doe", "Manila", LocalDate.of(1990, 1, 1));
        customerRepository.save(original);

        Customer updated = new Customer("123", "ACC001", "Jane", "Smith", "Cebu", LocalDate.of(1990, 1, 1));

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
        Customer customer = new Customer("123", "ACC001", "John", "Doe", "Manila", LocalDate.of(1990, 1, 1));
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
        Customer invalidCustomer = new Customer("124", "ACC002", "", "Doe", "Manila", LocalDate.of(1990, 1, 1));

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
                .andExpect(status().isNotFound()); // Task 1.5 Rule: 404 Not Found
    }

    @Test
    void testUpdateCustomer_NotFound_Returns400() throws Exception {
        // This covers the missing 50% branch in your Service class
        Customer updatedCustomer = new Customer("999", "ACC999", "Ghost", "User", "Nowhere", LocalDate.of(1990, 1, 1));

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
        // This explicitly calls your entity methods to satisfy JaCoCo's line counters
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