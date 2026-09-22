package com.bpi.customerservice;

import com.bpi.customerservice.entity.Customer;
import com.bpi.customerservice.repository.AuditLogRepository;
import com.bpi.customerservice.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("java:S5786")
public abstract class CustomerserviceApplicationTests {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected CustomerRepository customerRepository;

	@Autowired
	protected AuditLogRepository auditLogRepository;

	// Inject your RestTemplate (or WebServiceTemplate if you are using Spring WS)
	@Autowired
	protected RestTemplate restTemplate;

	// Protected so all child test classes can configure mock responses
	protected MockRestServiceServer mockServer;
	protected Customer defaultCustomer;

	@BeforeEach
	void setUp() {
		mockServer = MockRestServiceServer.createServer(restTemplate);

		customerRepository.deleteAll();
		auditLogRepository.deleteAll();

		defaultCustomer = Customer.builder()
				.customerNumber("091230")
				.accountNumber("1234567890")
				.firstName("Default")
				.lastName("Customer")
				.address("Makati")
				.birthDate(LocalDate.of(2000, 1, 1))
				.build();

		customerRepository.save(defaultCustomer);
	}

	@AfterEach
	void tearDown() {

		customerRepository.deleteAll();
		auditLogRepository.deleteAll();
		mockServer.reset();
	}

}